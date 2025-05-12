$input v_color0, v_color1, v_fog, v_refl, v_texcoord0, v_lightmapUV, v_extra, v_position

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);

uniform vec4 FogColor;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogAndDistanceControl;

float rain = mix(smoothstep(0.66, 0.3, FogAndDistanceControl.x), 0.0, step(FogAndDistanceControl.x, 0.0));

#ifdef SHADOW_ENABLED

float dirlight(vec3 normal, float rain, float night) {
    float shadowStrength = 1.5; // Adjust: >1.0 for darker shadows, <1.0 for brighter shadows
    float dirfac = mix(0.5, 0.25, night);
    dirfac = mix(dirfac, 0.0, rain);
    float dir = 1.0 - (dirfac * shadowStrength) * abs(normal.z); // Z-axis
    return dir;
}
#endif

#ifdef RAIN_ENABLED
// Define DROP_NUMBER before using it
#define DROP_NUMBER 14

float stickyRaindrop(vec2 uv, vec2 center, float baseSize) {
    vec2 p = (uv - center) / baseSize;
    float d = length(p);
    return smoothstep(0.8, 0.1, d);
}

vec3 RainDrop(vec4 diffuse, float time, vec2 uv) {
    vec3 baseColor = diffuse.rgb;
    vec3 kol = baseColor;
    const int drops = DROP_NUMBER; // Now DROP_NUMBER is defined above

    for (int i = 0; i < drops; i++) {
        float fi = float(i);
        vec2 dropPos = vec2(
            fract(sin(fi * 12.9898) * 43758.5453),
            fract(sin(fi * 78.233) * 12345.678 + time * 0.1)
        );
        dropPos.y = mod(dropPos.y - time * 0.12, 1.0);
        float baseSize = (0.03 + 0.015 * fract(sin(fi * 5.21) * 1000.0)) * (2460.0 / 1650.0);
        float dropMask = stickyRaindrop(uv, dropPos, baseSize);
        vec3 dropColor = vec3(0.9, 0.9, 0.9);
        kol = mix(kol, dropColor, dropMask * 0.55);
    }
    return kol;
}
#endif

void main() {
    #if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY) || defined(INSTANCING)
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        return;
    #endif

    vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);
    vec4 color = v_color0;

    #ifdef ALPHA_TEST
        if (diffuse.a < 0.6) {
            discard;
        }
    #endif

    #if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
        diffuse.rgb *= mix(vec3(1.0, 1.0, 1.0), texture2D(s_SeasonsTexture, v_color1.xy).rgb * 2.0, v_color1.z);
    #endif

    vec3 glow = nlGlow(s_MatTexture, v_texcoord0, v_extra.a);

    diffuse.rgb *= diffuse.rgb;

    vec3 lightTint = texture2D(s_LightMapTexture, v_lightmapUV).rgb;
    lightTint = mix(lightTint.bbb, lightTint * lightTint, 0.35 + 0.65 * v_lightmapUV.y * v_lightmapUV.y * v_lightmapUV.y);

    color.rgb *= lightTint;

    #if defined(TRANSPARENT) && !(defined(SEASONS) || defined(RENDER_AS_BILLBOARDS))
        if (v_extra.b > 0.9) {
            diffuse.rgb = vec3_splat(1.0 - NL_WATER_TEX_OPACITY * (1.0 - diffuse.b * 1.8));
            diffuse.a = color.a;
        }
    #else
        diffuse.a = 1.0;
    #endif

    diffuse.rgb *= color.rgb;
    diffuse.rgb += glow;

    if (v_extra.b > 0.9) {
        diffuse.rgb += v_refl.rgb * v_refl.a;
    } else if (v_refl.a > 0.0) {
        float dy = abs(dFdy(v_extra.g));
        if (dy < 0.0002) {
            float mask = v_refl.a * (clamp(v_extra.r * 10.0, 8.2, 8.8) - 7.8);
            diffuse.rgb *= 1.0 - 0.6 * mask;
            diffuse.rgb += v_refl.rgb * mask;
        }
    }

    #ifdef SHADOW_ENABLED

    float day = pow(max(min(1.0 - FogColor.r * 1.2, 1.0), 0.0), 0.4);
    float night = pow(max(min(1.0 - FogColor.r * 1.5, 1.0), 0.0), 1.2);
    float dusk = max(FogColor.r - FogColor.b, 0.0);
    vec3 N = normalize(cross(dFdx(v_position), dFdy(v_position)));
    diffuse.rgb *= dirlight(N, rain, night);

    #endif

    diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);

    #ifdef RAIN_ENABLED
    float time = ViewPositionAndTime.w;
    if (rain > 0.0) {
        vec2 uv = gl_FragCoord.xy / vec2(1080.0, 2460.0);
        uv.x -= 0.7;
        diffuse.rgb += RainDrop(diffuse, time, uv) * rain;
    }
    #endif

    diffuse.rgb = colorCorrection(diffuse.rgb);

    gl_FragColor = diffuse;
}