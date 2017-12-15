package edu.harvard.i2b2.crypto.util;

import java.util.ArrayList;

import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

public class Roles {
	private static Roles thisInstance;
	public static final String totalNumsRoleName = "TOTALNUMS";
	public static final String totalNumsNoisyRoleName = "TOTALNUMS_NOISY";
	public static final String totalNumsCumulRoleName = "TOTALNUMSCUMUL";
	public static final String totalNumsCumulNoisyRoleName = "TOTALNUMSCUMUL_NOISY";
	public static final String totalNumsTimeRoleName = "TOTALNUMSTIME";
	public static final String totalNumsTimeNoisyRoleName = "TOTALNUMSTIME_NOISY";

	   static {
           thisInstance = new Roles();
   }
   
   public static Roles getInstance() {
       return thisInstance;
   }

   public boolean isRoleValid(ProjectType projectInfo){
 
	   ArrayList<String> roles = (ArrayList<String>) projectInfo.getRole();
	   for(String param :roles) {
		   // Bug 728; enable feature for role = editor only
		//   if(param.equalsIgnoreCase("manager")) 
		//	   return true;
		 //  if(param.equalsIgnoreCase("admin")) 
		//	   return true;
		   if(param.equalsIgnoreCase("editor")) 
			   return true;
	   }
	   return false;
   }
   
   public boolean isRoleAdmin(ProjectType projectInfo) { 

	   ArrayList<String> roles = (ArrayList<String>) projectInfo.getRole();
	   for(String param :roles) {
		   // Bug 728; enable feature for role = editor only
		//   if(param.equalsIgnoreCase("manager")) 
		//	   return true;
		 
		   if(param.equalsIgnoreCase("admin")) { 
			   return true;
		   }
	   }
	   return false;
   }
   
   public boolean isRoleOfuscated(ProjectType projectInfo) { 
	   ArrayList<String> roles = (ArrayList<String>) projectInfo.getRole();
	   for(String param :roles) {
		   if (param.equalsIgnoreCase("DATA_AGG")) { 
			   return false;
		   }
	   }
	   return true;
   }
	
}
