#version 430

out vec4 fragColor;

uniform vec3 outlineColor = vec3(0.0, 0.0, 0.0); // Default black outline

void main(void) {
    fragColor = vec4(outlineColor, 1.0);
}