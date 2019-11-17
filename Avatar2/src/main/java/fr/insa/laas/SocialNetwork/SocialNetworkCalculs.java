package fr.insa.laas.SocialNetwork;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.insa.laas.Avatar2.MetaAvatar;

public class SocialNetworkCalculs {

	/*	------------------------------------ 		Co-work Object Relationship (C-WOR) 		---------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/

	
    /**		
     * Calculates the cosine similarity for two given vectors.
     * @param leftVector left vector
     * @param rightVector right vector
     * @return cosine similarity between the two vectors
     */
	
    public static Double CoWorkSimilarity(final Map<String, Double> leftVector, final Map<String, Double> rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }

        final Set<String> intersection = getIntersection(leftVector, rightVector);
        //System.out.println("[intersection]"+intersection);

        final double dotProduct = dot(leftVector, rightVector, intersection);
        double d1 = 0.0d;
        for (final Double value : leftVector.values()) {
            d1 += Math.pow(value, 2);
        }
        //System.out.println("[dotProduct]"+dotProduct);

        double d2 = 0.0d;
        for (final Double value : rightVector.values()) {
            d2 += Math.pow(value, 2);
        }
        double cosineSimilarity;
        if (d1 <= 0.0 || d2 <= 0.0) {
            cosineSimilarity = 0.0;

        } else {
            cosineSimilarity = (double) (dotProduct / (double) (Math.sqrt(d1) * Math.sqrt(d2)));
        }
        return cosineSimilarity;
    }

    /**		//Aux. method to calculate the cosine similarity
     * Returns a set with strings common to the two given maps.
     * @param leftVector left vector map
     * @param rightVector right vector map
     * @return common strings
     */
    private static Set<String> getIntersection(final Map<String, Double> leftVector,
            final Map<String, Double> rightVector) {
        final Set<String> intersection = new HashSet<>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    
    /**		//Aux. method to calculate the cosine similarity
     * Computes the dot product of two vectors. It ignores remaining elements. It means
     * that if a vector is longer than other, then a smaller part of it will be used to compute
     * the dot product.
     * @param leftVector left vector
     * @param rightVector right vector
     * @param intersection common elements
     * @return the dot product
     */
    private static double dot(final Map<String, Double> leftVector, final Map<String, Double> rightVector,
            final Set<String> intersection) {
        double dotProduct = 0;
        for (final String key : intersection) {
            
            dotProduct += leftVector.get(key) * rightVector.get(key);
            //System.out.println("[dot keys] "+leftVector.get(key)+" *  "+rightVector.get(key)+" = "+dotProduct);
        }
        
        return dotProduct;
    }
    

	/*	----------------------------------- 		Co-Location Objects Relationship (C-LOR)  		---------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
    //distanceVolOiseau
    public static double CoLocSimilarity (double LatA,double LongA,double LatB,double LongB) {
    
    LatA = Math.toRadians(LatA);
    LongA = Math.toRadians(LongA);
    LatB = Math.toRadians(LatB);
    LongB = Math.toRadians(LongB);
    double d = 6371 * Math.acos(Math.cos(LatA) * Math.cos(LatB) * Math.cos(LongB - LongA)+Math.sin(LatA) * Math.sin(LatB));
     return d;
    }
    

	/*	---------------------------------- 		Ownership Object Relationship (OOR) 		------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
    
    public static int OwnershipSimilarity (String user1,String user2) {
        
         if (user1.equals(user2)) return 1;
         else return 0;
   
        }  

	/*	------------------------------------------ 		SOCIAL DISTANCE 		-----------------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	   
  public static double SocialDistance(MetaAvatar metaAvatar, Map<String, Double> InterestsVectorB,double LatB, double LongB, String userB) {
  
	  	//System.out.println("Vect of A1: "+metaAvatar.getInterestsVector().toString()+", and its friend's: "+InterestsVectorB.toString());
        double coWorkDistance=CoWorkSimilarity(InterestsVectorB,metaAvatar.getInterestsVector());
        int ownerDistance=OwnershipSimilarity(userB, metaAvatar.getOwner());
        double coLocDistance=CoLocSimilarity(metaAvatar.getLatitude(),metaAvatar.getLongitude(),LatB,LongB);
        double socialDistance = 0.4*coWorkDistance+ 0.4*(1/coLocDistance)+ 0.2*ownerDistance;
        //System.out.println("[SocialDist]From "+metaAvatar.getName()+" CWD= "+coWorkDistance+", OD= "+ownerDistance+", CLD= "+(1/coLocDistance)+" ==> SD= "+socialDistance);
        return socialDistance;
  }
  
  //Social Distance relative to a interest, the same as the normal one, but concerning the Cowork Distance, we just use the level of interest (which is also between 0 and 1) 
  public static double SocialDistanceInterest(MetaAvatar metaAvatar, MetaAvatar metaFriend, double interestLevel) {
	  
	  //System.out.println("Vect of A1: "+metaAvatar.getInterestsVector().toString()+", and its friend's: "+InterestsVectorB.toString());
      int ownerDistance=OwnershipSimilarity(metaFriend.getOwner(), metaAvatar.getOwner());
      double coLocDistance=CoLocSimilarity(metaAvatar.getLatitude(),metaAvatar.getLongitude(),metaFriend.getLatitude(),metaFriend.getLongitude());
      double socialDistance = 0.4*interestLevel+ 0.4*(1/coLocDistance)+ 0.2*ownerDistance;
      //System.out.println("[SocialDistINTEREST]"+metaAvatar.getName()+" CWD= "+interestLevel+", OD= "+ownerDistance+", CLD= "+(1/coLocDistance)+" ==> SD= "+socialDistance);
      return socialDistance;
}
  
}
