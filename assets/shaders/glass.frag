#version 430

in vec2 tc;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingViewDir;
in vec3 varyingReflectVec;
in vec4 shadow_coord;
in mat3 TBN;

out vec4 fragColor;

struct PositionalLight
{   vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};
struct Material
{   vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
    float alpha;  // for transparency
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

// Reflection uniforms
uniform float refractionIndex = 1.52; // For glass
uniform float fresnelBias = 0.1;      // Controls Fresnel effect strength
uniform float fresnelScale = 1.0;     // Scales Fresnel effect
uniform float fresnelPower = 2.0;     // Sharpness of Fresnel effect

layout (binding = 0) uniform sampler2D samp;
layout (binding = 1) uniform sampler2DShadow shadowTex;
layout (binding = 2) uniform sampler2D normalMap;
layout (binding = 3) uniform samplerCube skyboxTex;

float lookup(float x, float y)
{   float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                        y * 0.001 * shadow_coord.w,
                                                        -0.01, 0.0));
    return t;
}

void main(void) {
    float shadowFactor = 0.0;
    float gamma = 2.2;
    
    // Get normalized vectors
    vec3 normalMapValue = texture(normalMap, tc).rgb * 2.0 - 1.0;
    vec3 N = normalize(TBN * normalMapValue);
    vec3 L = normalize(varyingLightDir);
    vec3 V = normalize(-varyingViewDir);
    vec3 H = normalize(L + V);
    
    // Shadow calculation
    float swidth = 2.5;
    vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
    shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
    shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
    shadowFactor = shadowFactor / 4.0;
    
    // Fresnel calculation (angle-dependent reflection)
    float fresnel = fresnelBias + fresnelScale * pow(1.0 + dot(N, V), fresnelPower);
    fresnel = clamp(fresnel, 0.0, 1.0);
    
    // Reflection from environment/skybox
    vec3 reflectionColor = texture(skyboxTex, varyingReflectVec).rgb;
    
    // Refraction calculation
    vec3 refractVec = refract(-V, N, 1.0/refractionIndex);
    vec3 refractionColor = texture(skyboxTex, refractVec).rgb;
    
    // Standard lighting calculation
    float cosTheta = max(dot(L, N), 0.0);
    float cosPhi = max(dot(H, N), 0.0);
    
    vec3 textureColor = pow(texture(samp, tc).rgb, vec3(gamma));
    vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz * textureColor;
    vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * cosTheta * textureColor;
    vec3 specular = light.specular.xyz * material.specular.xyz * pow(cosPhi, material.shininess * 3.0);
    
    // Combine lighting with reflections and refractions
    vec3 finalColor = ambient + shadowFactor * (diffuse + specular);
    
    // Mix based on fresnel (more reflective at glancing angles)
    finalColor = mix(
        mix(finalColor, refractionColor, 0.5), // Mix lighting with refraction
        reflectionColor,                       // Reflection component
        fresnel                                // Fresnel factor
    );
    
    // Apply gamma correction
    finalColor = pow(finalColor, vec3(1.0/gamma));
    
    // Output with transparency
    fragColor = vec4(finalColor, material.alpha);
}