precision mediump float;

uniform sampler2D u_TextureUnit;
varying vec2 v_Texture;

uniform float contrast;

void main()
{
    vec4 inColor = texture2D(u_TextureUnit, v_Texture);
    vec3 nCol = normalize(inColor.rgb);
    inColor.r += contrast;
    gl_FragColor = vec4(nCol.rgb,inColor.a);
}