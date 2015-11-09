package edu.gsu.hxue.utilities;

public class ShowSMCResults {
    public static void main(String[] args) {
        System.out.println("Performing utility...");
        //PresentationUtility.drawResultsFromSerializedResults( 10800, 50, "C:/Users/haydon/Desktop/SMCResults/Case4SenSim_s1000" );
        PresentationUtility.drawResultFrontsFromSerializedResults(10800, 50, "C:/Users/haydon/Desktop/SMCResults/Case4Bootstrap_s1000");
        System.out.println("Done!");
    }
}
