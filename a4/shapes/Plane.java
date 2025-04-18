package a4.shapes;

// Primitive shape with six vertices, used as ground and wall plane

public class Plane {
    float[] vertices = {
        -1.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 1.0f,
		-1.0f, 0.0f, -1.0f,
		-1.0f, 0.0f, -1.0f,
		1.0f, 0.0f, 1.0f,
		1.0f, 0.0f, -1.0f
    };

    float[] texCoords = {
        0.0f, 0.0f,  1.0f, 0.0f,  0.0f, 1.0f,
		0.0f, 1.0f,  1.0f, 0.0f,  1.0f, 1.0f
    };

    float[] normals = {	
        0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f
	};

    float[] tangents = {
        1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f
    };
    
    float[] bitangents = {
        0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f
    };

    public Plane(){}
    public float[] getVertices() { return vertices; }
    public float[] getTexCoords() { return texCoords; }
    public float[] getNormals() { return normals; }
    public float[] getTangents() { return tangents; }
    public float[] getBitangents() { return bitangents; }

}
