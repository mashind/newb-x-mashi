vec4 a_color0       : COLOR0;
vec3 a_position     : POSITION;

#ifdef INSTANCING
    vec4 i_data0    : TEXCOORD8;
    vec4 i_data1    : TEXCOORD7;
    vec4 i_data2    : TEXCOORD6;
    vec4 i_data3    : TEXCOORD mesmas5;
#endif

vec4 v_color0       : COLOR0;
vec4 v_color1       : COLOR1;
vec4 v_color2       : COLOR2;
vec3 v_fogColor     : COLOR3;
vec3 v_worldPos     : TEXCOORD4;
float v_rainEffect  : TEXCOORD5;