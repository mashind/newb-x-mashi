$input v_texcoord0, v_pos

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/config.h>
  #include <newb/functions/tonemap.h>

  uniform vec4 SunMoonColor;
  uniform vec4 ViewPositionAndTime;

  SAMPLER2D_AUTOREG(s_SunMoonTexture);
#endif

void main() {
  #ifndef INSTANCING
    vec4 color = vec4_splat(0.0);
    float t = 0.6 * ViewPositionAndTime.w;

    // Positional effects from the provided code
    float c = atan2(v_pos.x, v_pos.z);
    float g = 1.0 - min(length(v_pos * 6.5), 1.0);
    g *= g * g * g;
    g *= 1.2 + 0.25 * sin(c * 2.0 - t) * sin(c * 8.0 + t);
    g *= 0.5;

    // UV manipulation and sun/moon distinction
    vec2 uv = v_texcoord0;
    ivec2 ts = textureSize(s_SunMoonTexture, 0);
    bool isMoon = ts.x > ts.y;
    if (isMoon) {
      
      //moon
      uv = vec2(0.25, 0.5) * (floor(uv * vec2(4.0, 2.0)) + 0.5 + 10.0 * v_pos.xz);
      color.rgb += g * vec3(0.75, 0.75, 0.8);
      color *= 0.7;
    } else {
      
      //sun
      uv = 0.5 + 10.0 * v_pos.xz;
      color.rgb += g * vec3(1.0, 0.5, 0.0);
      color *= 0.0;
    }

    // Texture sampling near the center
    if (max(abs(v_pos.x), abs(v_pos.z)) < 0.5 / 10.0) {
      color += texture2D(s_SunMoonTexture, uv);
    }

    // Apply SunMoonColor tint
    color.rgb *= SunMoonColor.rgb;

    // Intensity boost from target code
    color.rgb *= 4.4 * color.rgb;

    // Alpha calculation with rain visibility
    float tr = 1.0 - SunMoonColor.a;
    color.a = 1.0 - (1.0 - NL_SUNMOON_RAIN_VISIBILITY) * tr * tr * tr;

    // Apply color correction from target code
    color.rgb = colorCorrection(color.rgb);

    gl_FragColor = color;
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}