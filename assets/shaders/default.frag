#version 430

in vec2 tc;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 shadow_coord;

out vec4 fragColor;

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;

uniform int tileCount;
uniform int useTexture;
uniform vec3 axisColor;

layout (binding = 0) uniform sampler2D samp;
layout (binding = 1) uniform sampler2DShadow shadowTex;

void main(void) {
    if (useTexture == 1) {
        float gamma = 2.2;
        vec3 textureColor = pow(texture(samp, tc).rgb, vec3(gamma));

        vec3 L = normalize(varyingLightDir);
        vec3 N = normalize(varyingNormal);
        vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);
        vec3 H = normalize(varyingHalfVector);
        
        float cosTheta = dot(L,N);
        float cosPhi = dot(H,N);

        float notInShadow = textureProj(shadowTex, shadow_coord);

        vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz * textureColor.xyz;
        vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0) * textureColor.xyz;
        vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
        
        fragColor = vec4(ambient, 1.0);

        if (notInShadow == 1.0) {
            fragColor = vec4((ambient + diffuse + specular), 1.0);
        }

        fragColor.rgb = pow(fragColor.rgb, vec3(1.0/gamma));
    } else {
        fragColor = vec4(axisColor, 1.0);
    }
}