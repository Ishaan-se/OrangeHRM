package com.orangehrm.utilities;

import java.util.List;

import org.testng.annotations.DataProvider;

public class DataProviders {
	
	private static final String FILE_PATH = System.getProperty("user.dir")+ "/src/test/resources/testdata/TestData.xlsx";

	
	@DataProvider(name="validLoginData")
	public static Object[][] validLoginData(){
		return getSheetData("validLoginData");
	}
	
	@DataProvider(name="inValidLoginData")
	public static Object[][] inValidLoginData(){
		return getSheetData("inValidLoginData");
	}
	
	@DataProvider(name="emplVerification")
	public static Object[][] emplVerfication(){
		return getSheetData("emplVerfication");
	}
	
	
	
	private static Object[][] getSheetData(String sheetName) {
		List<String[]> sheetData = ExcelReaderUtility.getSheetData(FILE_PATH, sheetName);
		
		Object[][] data =  new Object[sheetData.size()][sheetData.get(0).length];
		
		for(int i=0; i<sheetData.size(); i++) {
			data[i] = sheetData.get(i);
			if ("validLoginData".equals(sheetName)) {
				data[i][0] = resolveCredential(data[i][0], "orangehrm.username", "ORANGEHRM_USERNAME");
				data[i][1] = resolveCredential(data[i][1], "orangehrm.password", "ORANGEHRM_PASSWORD");
			}
		}
		
		return data;
	}

	private static String resolveCredential(String value, String systemProperty, String environmentVariable) {
		if (!value.startsWith("${") || !value.endsWith("}")) {
			return value;
		}

		String credential = System.getProperty(systemProperty, System.getenv(environmentVariable));
		if (credential == null || credential.isBlank()) {
			throw new IllegalStateException("Set " + environmentVariable
					+ " or -D" + systemProperty + " to run valid-login tests.");
		}
		return credential;
	}
	
}
