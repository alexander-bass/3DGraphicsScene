#version 430

layout (location = 0) in vec3 vertPos;

uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform float outlineScale = 1.02; // Scale factor for the outline

void main(void) {
    // Scale the vertex position along its normal direction
    vec3 scaledPos = vertPos * outlineScale;
    
    // Apply transformations
    gl_Position = p_matrix * v_matrix * m_matrix * vec4(scaledPos, 1.0);
}