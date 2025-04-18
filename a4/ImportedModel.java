package a4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import org.joml.*;
import org.joml.Math;

// ImportedModel.java from textbook

public class ImportedModel
{
	private Vector3f[] vertices;
	private Vector2f[] texCoords;
	private Vector3f[] normals;
	private Vector3f[] tangents;
	private Vector3f[] bitangents;

	private int numVertices;

	public ImportedModel(String filename) {
		ModelImporter modelImporter = new ModelImporter();
		try {
			modelImporter.parseOBJ(filename);
			numVertices   = modelImporter.getNumVertices();
			float[] verts = modelImporter.getVertices();
			float[] tcs   = modelImporter.getTextureCoordinates();
			float[] norm  = modelImporter.getNormals();

			vertices = new Vector3f[numVertices];
			texCoords = new Vector2f[numVertices];
			normals = new Vector3f[numVertices];
			tangents = new Vector3f[numVertices];
			bitangents = new Vector3f[numVertices];
			
			for(int i=0; i<vertices.length; i++) {
				vertices[i] = new Vector3f();
				vertices[i].set(verts[i*3], verts[i*3+1], verts[i*3+2]);
				texCoords[i] = new Vector2f();
				texCoords[i].set(tcs[i*2], tcs[i*2+1]);
				normals[i] = new Vector3f();
				normals[i].set(norm[i*3], norm[i*3+1], norm[i*3+2]);
				tangents[i] = new Vector3f(0.0f, 0.0f, 0.0f);
            	bitangents[i] = new Vector3f(0.0f, 0.0f, 0.0f);
			}

			computeTangentSpace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private void computeTangentSpace() {
		// Process each triangle (every 3 vertices)
		for (int i = 0; i < numVertices; i += 3) {
			Vector3f v0 = vertices[i];
			Vector3f v1 = vertices[i+1];
			Vector3f v2 = vertices[i+2];
			
			Vector2f uv0 = texCoords[i];
			Vector2f uv1 = texCoords[i+1];
			Vector2f uv2 = texCoords[i+2];
			
			// Calculate edges and UV differences
			Vector3f edge1 = new Vector3f(v1).sub(v0);
			Vector3f edge2 = new Vector3f(v2).sub(v0);
			
			Vector2f deltaUV1 = new Vector2f(uv1).sub(uv0);
			Vector2f deltaUV2 = new Vector2f(uv2).sub(uv0);
			
			// Calculate tangent and bitangent using the formula:
			// T = (E1 * dV2 - E2 * dV1) / (dU1 * dV2 - dU2 * dV1)
			// B = (E2 * dU1 - E1 * dU2) / (dU1 * dV2 - dU2 * dV1)
			float det = deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y;
			
			if (Math.abs(det) < 0.000001f) {
				// Degenerate case - use an arbitrary tangent
				tangents[i] = new Vector3f(1.0f, 0.0f, 0.0f);
				tangents[i+1] = new Vector3f(1.0f, 0.0f, 0.0f);
				tangents[i+2] = new Vector3f(1.0f, 0.0f, 0.0f);
				
				bitangents[i] = new Vector3f(0.0f, 1.0f, 0.0f);
				bitangents[i+1] = new Vector3f(0.0f, 1.0f, 0.0f);
				bitangents[i+2] = new Vector3f(0.0f, 1.0f, 0.0f);
				continue;
			}
			
			float invDet = 1.0f / det;
			
			Vector3f tangent = new Vector3f(
				(edge1.x * deltaUV2.y - edge2.x * deltaUV1.y) * invDet,
				(edge1.y * deltaUV2.y - edge2.y * deltaUV1.y) * invDet,
				(edge1.z * deltaUV2.y - edge2.z * deltaUV1.y) * invDet
			);
			
			Vector3f bitangent = new Vector3f(
				(edge2.x * deltaUV1.x - edge1.x * deltaUV2.x) * invDet,
				(edge2.y * deltaUV1.x - edge1.y * deltaUV2.x) * invDet,
				(edge2.z * deltaUV1.x - edge1.z * deltaUV2.x) * invDet
			);
			
			// Assign to all vertices of the triangle
			tangents[i] = tangent;
			tangents[i+1] = tangent;
			tangents[i+2] = tangent;
			
			bitangents[i] = bitangent;
			bitangents[i+1] = bitangent;
			bitangents[i+2] = bitangent;
		}
		
		// Orthogonalize tangent vectors for each vertex (Gram-Schmidt process)
		for (int i = 0; i < numVertices; i++) {
			Vector3f n = normals[i];
			Vector3f t = tangents[i];
			
			// Gram-Schmidt orthogonalization
			t.sub(n.mul(n.dot(t), new Vector3f())).normalize();
			
			// Calculate handedness
			Vector3f b = bitangents[i];
			Vector3f temp = new Vector3f();
			n.cross(t, temp);
			float handedness = (temp.dot(b) < 0.0f) ? -1.0f : 1.0f;
			
			// Adjust tangent if needed
			if (handedness < 0.0f) {
				t.mul(-1.0f);
			}
		}
	}


	public int getNumVertices() { return numVertices; }
	public Vector3f[] getVertices() { return vertices; }
	public Vector2f[] getTexCoords() { return texCoords; }	
	public Vector3f[] getNormals() { return normals; }	
	public Vector3f[] getTangents() { return tangents; }
	public Vector3f[] getBitangents() { return bitangents; }

	private class ModelImporter
	{	// values as read from OBJ file
		private ArrayList<Float> vertVals = new ArrayList<Float>();
		private ArrayList<Float> triangleVerts = new ArrayList<Float>(); 
		private ArrayList<Float> textureCoords = new ArrayList<Float>();

		// values stored for later use as vertex attributes
		private ArrayList<Float> stVals = new ArrayList<Float>();
		private ArrayList<Float> normals = new ArrayList<Float>();
		private ArrayList<Float> normVals = new ArrayList<Float>();

		public void parseOBJ(String filename) throws IOException
		{	InputStream input = new FileInputStream(new File(filename));
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = br.readLine()) != null)
			{	if(line.startsWith("v "))			// vertex position ("v" case)
				{	for(String s : (line.substring(2)).split(" "))
					{	vertVals.add(Float.valueOf(s));
				}	}
				else if(line.startsWith("vt"))			// texture coordinates ("vt" case)
				{	for(String s : (line.substring(3)).split(" "))
					{	stVals.add(Float.valueOf(s));
				}	}
				else if(line.startsWith("vn"))			// vertex normals ("vn" case)
				{	for(String s : (line.substring(3)).split(" "))
					{	normVals.add(Float.valueOf(s));
				}	}
				else if(line.startsWith("f"))			// triangle faces ("f" case)
				{	for(String s : (line.substring(2)).split(" "))
					{	String v = s.split("/")[0];
						String vt = s.split("/")[1];
						String vn = s.split("/")[2];
	
						int vertRef = (Integer.valueOf(v)-1)*3;
						int tcRef   = (Integer.valueOf(vt)-1)*2;
						int normRef = (Integer.valueOf(vn)-1)*3;
	
						triangleVerts.add(vertVals.get(vertRef));
						triangleVerts.add(vertVals.get((vertRef)+1));
						triangleVerts.add(vertVals.get((vertRef)+2));

						textureCoords.add(stVals.get(tcRef));
						textureCoords.add(stVals.get(tcRef+1));
	
						normals.add(normVals.get(normRef));
						normals.add(normVals.get(normRef+1));
						normals.add(normVals.get(normRef+2));
			}	}	}
			input.close();
		}

		public int getNumVertices() { return (triangleVerts.size()/3); }

		public float[] getVertices()
		{	float[] p = new float[triangleVerts.size()];
			for(int i = 0; i < triangleVerts.size(); i++)
			{	p[i] = triangleVerts.get(i);
			}
			return p;
		}

		public float[] getTextureCoordinates()
		{	float[] t = new float[(textureCoords.size())];
			for(int i = 0; i < textureCoords.size(); i++)
			{	t[i] = textureCoords.get(i);
			}
			return t;
		}
	
		public float[] getNormals()
		{	float[] n = new float[(normals.size())];
			for(int i = 0; i < normals.size(); i++)
			{	n[i] = normals.get(i);
			}
			return n;
		}	
	}
}
