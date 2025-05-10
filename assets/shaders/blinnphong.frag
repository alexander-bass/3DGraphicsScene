#version 430

in vec2 tc;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 shadow_coord;
in mat3 TBN;
in vec3 vertEyeSpacePos;

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

// Fog uniforms
uniform vec3 fogColor;
uniform float fogStart;
uniform float fogEnd;
uniform bool fogEnabled;

layout (binding = 0) uniform sampler2D samp;
layout (binding = 1) uniform sampler2DShadow shadowTex;
layout (binding = 2) uniform sampler2D normalMap;

float lookup(float x, float y)
{  	float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                         y * 0.001 * shadow_coord.w,
                                                         -0.01, 0.0));
	return t;
}

void main(void) {
    float shadowFactor = 0.0;
    float gamma = 2.2;
    vec3 textureColor = pow(texture(samp, tc).rgb, vec3(gamma));

    vec3 normalMapValue = texture(normalMap, tc).rgb * 2.0 - 1.0;

    vec3 L = normalize(varyingLightDir);
    vec3 N = normalize(TBN * normalMapValue);
    vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);
    vec3 H = normalize(varyingHalfVector);

    // low res pcf
    float swidth = 2.5;
    vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
    shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor = shadowFactor / 4.0;

    float cosTheta = dot(L,N);
    float cosPhi = dot(H,N);

    vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz * textureColor.xyz;
    vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0) * textureColor.xyz;
    vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
        
    fragColor = vec4((ambient + shadowFactor * (diffuse + specular)), 1.0);

    fragColor.rgb = pow(fragColor.rgb, vec3(1.0/gamma));

    // Add fog calculation
    if (fogEnabled) {
        float dist = length(vertEyeSpacePos);
        float fogFactor = (fogEnd - dist) / (fogEnd - fogStart);
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        fragColor.rgb = mix(fogColor, fragColor.rgb, fogFactor);
    }
}