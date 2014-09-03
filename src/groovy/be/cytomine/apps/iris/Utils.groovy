package be.cytomine.apps.iris

import grails.converters.JSON
import groovy.json.JsonBuilder;

import org.codehaus.groovy.grails.web.json.JSONElement;
import org.json.simple.JSONObject

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import be.cytomine.client.Cytomine
import be.cytomine.client.collections.AnnotationCollection
import be.cytomine.client.models.Annotation
import be.cytomine.client.models.Ontology

class Utils {

	/**
	 * 
	 * @param cytomine the cytomine instance 
	 * @param params parameter map, required: imageID, projectID, userID
	 * @return
	 */
	public JSONObject getUserProgress(Cytomine cytomine, long projectID, long imageID, long userID){
		JSONObject jsonResult = new JSONObject();

		int totalAnnotations = 0
		int labeledAnnotations = 0

		// define the filter for the query
		Map<String,String> filters = new HashMap<String,String>()
		filters.put("project", String.valueOf(projectID))
		filters.put("image", String.valueOf(imageID))

		// get the annotations of this image
		AnnotationCollection annotations = cytomine.getAnnotations(filters)

		// total annotations in a given image
		totalAnnotations = annotations.size();

		// count the annotations per user
		for(int i=0;i<annotations.size();i++) {
			Annotation annotation = annotations.get(i)
			// grab all terms from all users for the current annotation
			List userByTermList = annotation.getList("userByTerm");
			for (assignment in userByTermList){
				List userList = assignment.get("user").toList()
				
				// if the user has assigned a label to this annotation, increase the counter
				if (userID in userList){
					labeledAnnotations++
				}
			}
		}

		jsonResult.put("projectID", projectID)
		jsonResult.put("imageID", imageID)
		jsonResult.put("userID", userID)
		jsonResult.put("labeledAnnotations", labeledAnnotations)
		jsonResult.put("totalAnnotations", totalAnnotations)
		// compute the progress in percent
		int userProgress = (totalAnnotations==0?0:(int)((labeledAnnotations/totalAnnotations)*100));
		jsonResult.put("userProgress", userProgress)

		// send the response to the client
		return jsonResult;
	}
	
	public List<JSONObject> flattenOntology(Ontology ontology){
		// perform recursion and flatten the hierarchy
		JSONObject root = ontology.getAttr();
		List flatHierarchy = new ArrayList<JSONObject>();
		
		// build a lookup table for each term in the annotation
		Map<Long, Object> dict = new HashMap<Long, Object>();
		
		// pass the root node
		flatHelper(root, dict, flatHierarchy);
		
//		String result = new Gson().toJson(flatHierarchy);
//		println "######################################################"
		
		return flatHierarchy;
	}
	
	private void flatHelper(JSONObject node, Map<Long,Object> dict, List flatHierarchy){
		// get the node's children
		List childrenList = node.get("children").toList()
		
		// recurse through the children
		for (child in childrenList){
			// put each node to the dictionary
			dict.put(Long.valueOf(child.get("id")), child);
			
			if (child.get("isFolder")){
				flatHelper(child, dict, flatHierarchy);
			} else {
				String parentName = "root";
				
				if (child.get("parent") != null){
					// these are the non-root elements
					parentName = dict.get(Long.valueOf(child.get("parent"))).get("name")
				} 
								
				// add the child to the flat ontology
				child.put("parentName", parentName)
				flatHierarchy.add(child);
			}
		}
	}
	
	public JSONElement modelToJSON(def object){
		return JSON.parse((object as JSON).toString())
	}
}
