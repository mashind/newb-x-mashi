$input a_color0, a_position
$output v_color0

#include <bgfx_shader.sh>

uniform vec4 StarsColor;

#ifndef WIN32
// GLSL code for Android or other OpenGL-based platforms
void main() {
    vec3 pos = a_position;
    vec3 worldPos = mul(u_model[0], vec4(pos, 1.0)).xyz;

    vec4 color = a_color0;
    color.rgb *= (0.6 + 0.4 * sin(2.0 * pos)) * 1.5; // 50% brighter
    color.rgb *= StarsColor.rgb;

    v_color0 = color;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));

    // *** Star Size Adjustment for GLSL ***
    gl_PointSize = 8.0; // Set the point size for rendering
}
#else
// HLSL code for Windows (Direct3D)
struct Output {
    float4 v_color0 : COLOR0;  // Output color
    float4 gl_Position : SV_POSITION;  // Vertex position
    float PointSize : SV_PointSize;  // Point size for Direct3D
};

Output main(float4 a_color0 : COLOR0, float3 a_position : POSITION) {
    Output _varying_;

    vec3 pos = a_position;
    vec3 worldPos = mul(u_model[0], vec4(pos, 1.0)).xyz;

    vec4 color = a_color0;
    color.rgb *= (0.6 + 0.4 * sin(2.0 * pos)) * 1.5; // 50% brighter
    color.rgb *= StarsColor.rgb;

    _varying_.v_color0 = color;
    _varying_.gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));

    // *** Star Size Adjustment for HLSL ***
    _varying_.PointSize = 8.0; // Set the point size for Direct3D

    return _varying_;
}
#endif