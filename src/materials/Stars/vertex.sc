$input a_color0, a_position
$output v_color0

#include <bgfx_shader.sh>

uniform vec4 StarsColor;

void main() {
#ifndef INSTANCING
  vec3 pos = a_position;
  vec3 worldPos = mul(u_model[0], vec4(pos, 1.0)).xyz;

  vec4 color = a_color0;

  float starFactor = fract(sin(dot(pos.xy, vec2(12.9898, 78.233))) * 43758.5453);
  float sizeFactor = mix(0.5, 1.5, starFactor); // Range of size between 0.5 and 1.5
  float brightnessFactor = mix(0.3, 2.0, starFactor); // Range from dim (0.3) to bright (2.0)

  if (brightnessFactor > 1.5) {
    color.rgb = vec3(1.0); // Bright white stars
  } else {
    color.rgb *= (0.6 + 0.4 * sin(2.0 * pos));
    color.rgb = min(color.rgb, vec3(1.0));
    color.rgb *= StarsColor.rgb;
  }

  color.rgb *= sizeFactor;

  v_color0 = color;
  gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
  gl_Position = vec4(0.0, 0.0, 0.0, 0.0);
#endif
}