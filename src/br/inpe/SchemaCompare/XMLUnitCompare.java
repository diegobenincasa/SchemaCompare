package br.inpe.SchemaCompare;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.ElementSelectors;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class XMLUnitCompare {
	
	Source baseFile, testFile;
	String baseSchema, testSchema, uniqueSchema;
	
	Integer numAdded 	  = 0,
			numRemoved 	  = 0,
			numRelocated  = 0,
			numRefactored = 0;
	
	boolean empty		  = true;
	
	Integer add_elements = 0;
	Integer add_attributes = 0;
	Integer add_imports = 0;
	Integer add_complextypes = 0;
	Integer rem_elements = 0;
	Integer rem_attributes = 0;
	Integer rem_imports = 0;
	Integer rem_complextypes = 0;
	Integer ref_elements = 0;
	Integer ref_attributes = 0;
	Integer ref_imports = 0;
	Integer rel_elements = 0;
	Integer rel_attributes = 0;
	Integer before_elements = 0;
	Integer before_attributes = 0;
	Integer before_imports = 0;
	Integer before_complextypes = 0;
	Integer after_elements = 0;
	Integer after_attributes = 0;
	Integer after_imports = 0;
	Integer after_complextypes = 0;
	
	public boolean init(String controlFile, String testFile) throws SAXException, IOException, ParserConfigurationException
	{
		//setFiles(controlFile, TestFile);
		baseSchema = controlFile;
		testSchema = testFile;
		empty = false;
		
		listAddAndRemove();
		setCounters();
		
		return !empty;
	}
	
	public boolean init(String unSchema, Integer ba) throws SAXException, IOException, ParserConfigurationException
	{
		uniqueSchema = unSchema;
		empty = false;
		
		if(ba == 1)
//		listAddAndRemove();
			setCountersBefore();
		else if(ba == 2)
			setCountersAfter();

		return !empty;
	}
	
	public boolean init(byte[] controlFile, byte[] TestFile) throws SAXException, IOException, ParserConfigurationException
	{
		setFilesSource(controlFile, TestFile);
		empty = false;
		
		listAddAndRemove();
		setCounters();
		
		return !empty;
	}
	
	private void setBaseFile(String fl)
	{
		baseFile = Input.fromFile(fl).build();
	}
	
	private void setTestFile(String fl)
	{
		testFile = Input.fromFile(fl).build();
	}
	
	private void setBaseFileSource(byte[] fl1)
	{
		baseFile = Input.fromByteArray(fl1).build();
	}
	
	private void setTestFileSource(byte[] fl2)
	{
		testFile = Input.fromByteArray(fl2).build();
	}
	
	public void setFiles(String fl1, String fl2)
	{
		setBaseFile(fl1);
		setTestFile(fl2);
	}
	
	public void setFilesSource(byte[] fl1, byte[] fl2)
	{
		setBaseFileSource(fl1);
		setTestFileSource(fl2);
	}
	
	public XMLUnitCompare(){
		// Default constructor
		empty = true;
	}
	
	public boolean isEmpty(){
		return empty;
	}
	
	public void listAddAndRemove(){
		Diff myDiff = DiffBuilder.compare(Input.fromString(baseSchema))
	              .withTest(Input.fromString(testSchema))
	              .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes))
	              .ignoreComments()
	              .ignoreWhitespace()
	              .checkForSimilar()
	              .build();	
									
		Iterable<Difference> diffs = myDiff.getDifferences();
		NormalizedLevenshtein nl = new NormalizedLevenshtein();
		Map<String, String> addedTags = new HashMap<String, String>();
		Map<String, String> removedTags = new HashMap<String, String>();
		Map<String, String> refactoredTags = new HashMap<String, String>();
		Map<String, String> relocatedTags = new HashMap<String, String>();
		
		if(!myDiff.hasDifferences())
		{
//			System.out.println("No differences found");
		}
		else
		{
			for (Difference difference : diffs) 
			{
				String nodeNameTest = "";
				String nodeTypeTest = "";
				String nodeNameControl = "";
				String nodeTypeControl = "";
				short nodeTypeT = -1;
				short nodeTypeC = -1;
				String nodeTType = "";
				String nodeTName = "";
				String nodeCType = "";
				String nodeCName = "";
				
				try
				{
					nodeTypeT = difference.getComparison().getTestDetails().getTarget().getNodeType();
/*					if(nodeTypeT == Node.ATTRIBUTE_NODE)
					{
						nodeTypeTest = difference.getComparison().getTestDetails().getTarget().getNodeName();
						if(nodeTypeTest == "name" || nodeTypeTest == "namespace")
						{
							nodeNameTest = difference.getComparison().getTestDetails().getTarget().getNodeValue();
							nodeTName = nodeNameTest;
						}
					}
*/					if(nodeTypeT == Node.ELEMENT_NODE)
					{
						nodeTypeTest = difference.getComparison().getTestDetails().getTarget().getNodeName();
						nodeTName = difference.getComparison().getTestDetails().getTarget().getAttributes().getNamedItem("name").toString();
						if(nodeTypeTest.contains("import"))
							nodeTName = difference.getComparison().getTestDetails().getTarget().getAttributes().getNamedItem("namespace").toString();
						if(nodeTypeTest.contains("element") || nodeTypeTest.contains("attribute") || nodeTypeTest.contains("complexType") || nodeTypeTest.contains("import"))
							nodeNameTest = 
							  nodeTypeTest
							+ " "
							+ nodeTName;
					}
					else
						continue;
				}
				catch(NullPointerException e)
				{
				}
				
				try
				{
					nodeTypeC = difference.getComparison().getControlDetails().getTarget().getNodeType();
/*					if(nodeTypeC == Node.ATTRIBUTE_NODE)
					{
						nodeTypeControl = difference.getComparison().getControlDetails().getTarget().getNodeName();
						if(nodeTypeControl == "name" || nodeTypeControl == "namespace")
						{
							nodeNameControl = difference.getComparison().getControlDetails().getTarget().getNodeValue();
							nodeCName = nodeNameControl;
						}
					}
*/					if(nodeTypeC == Node.ELEMENT_NODE)
					{
						nodeTypeControl = difference.getComparison().getControlDetails().getTarget().getNodeName();
						nodeCName = difference.getComparison().getControlDetails().getTarget().getAttributes().getNamedItem("name").toString();
						if(nodeTypeControl.contains("import"))
							nodeCName = difference.getComparison().getTestDetails().getTarget().getAttributes().getNamedItem("namespace").toString();
						if(nodeTypeControl.contains("element") || nodeTypeControl.contains("attribute") || nodeTypeControl.contains("complexType") || nodeTypeControl.contains("import"))
							nodeNameControl = 
								  nodeTypeControl
								+ " "
								+ nodeCName;
					}
					else
						continue;
				}
				catch(NullPointerException e)
				{
				}
				if(!nodeTypeControl.contains("complexType") && !nodeTypeTest.contains("complexType"))
				{
					if(nodeNameTest.isEmpty() && !nodeNameControl.isEmpty())
					{
						removedTags.put(nodeCName, nodeTypeControl);
					}
					else if(!nodeNameTest.isEmpty() && nodeNameControl.isEmpty())
					{
						addedTags.put(nodeTName, nodeTypeTest);
					}
					else if(nodeNameTest != nodeNameControl)
					{
						removedTags.put(nodeCName, nodeTypeControl);
						addedTags.put(nodeTName, nodeTypeTest);
					}
				}
				else
				{
					if(nodeTypeControl.isEmpty() && !nodeTypeTest.isEmpty())
					{
						add_complextypes++;
					}
					else if(!nodeTypeControl.isEmpty() && nodeTypeTest.isEmpty())
					{
						rem_complextypes++;
					}
				}
			}
			
			double sim;
			boolean found = false;
			String matchKey = "";
			
			for (String key : removedTags.keySet()) {
				found = false;
				for (String key2 : addedTags.keySet())
				{
					sim = nl.distance(key, key2);
					if(sim > 0 && sim < 0.1)
					{
						found = true;
						matchKey = key2;
						break;
					}
					else if(sim == 0)
					{
						if(removedTags.get(key).contains("element") && removedTags.get(key) == addedTags.get(key2))
						{
							rel_elements++;
						//	rem_elements--;
						//	add_elements--;
						}
						else if(removedTags.get(key).contains("attribute") && removedTags.get(key) == addedTags.get(key2))
						{
							rel_attributes++;
						//	rem_attributes--;
						//	add_attributes--;
						}
						else if(removedTags.get(key).contains("import") && removedTags.get(key) == addedTags.get(key2))
						{
						//	rem_imports--;
						//	add_imports--;
						}
//						System.out.println("Tag " + removed.get(key) + " " + key + " is just realocated inside schema.");
						numRelocated++;
						break;
					}
						
				}
				if(found)
				{
					if(removedTags.get(key).contains("element"))
					{
						ref_elements++;
					//	rem_elements--;
					//	add_elements--;
					}
					else if(removedTags.get(key).contains("attribute"))
					{
						ref_attributes++;
					//	rem_attributes--;
					//	add_attributes--;
					}
					else if(removedTags.get(key).contains("import"))
					{
						ref_imports++;
					//	rem_imports--;
					//	add_imports--;
					}
//					System.out.println("Tag " + removed.get(key) + " " + key
//							+ " refactored to "
//							+ added.get(matchKey) + " " + matchKey);
					numRefactored++;
				}
				else
				{
					if(removedTags.get(key).contains("element"))
					{
						rem_elements++;
					}
					else if(removedTags.get(key).contains("attribute"))
					{
						rem_attributes++;
					}
					else if(removedTags.get(key).contains("import"))
					{
						rem_imports++;
					}
//					System.out.println("Tag " + removed.get(key) + " " + key + " removed.");
					numRemoved++;
				}
			}

			for (String key : addedTags.keySet()) {
				found = false;
				for (String key2 : removedTags.keySet()) {
					sim = nl.distance(key, key2);
					if(sim < 0.1)
					{
						found = true;
						matchKey = key2;
						break;
					}
				}
				if(!found)
				{
					if(addedTags.get(key).contains("element"))
					{
						add_elements++;
					}
					else if(addedTags.get(key).contains("attribute"))
					{
						add_attributes++;
					}
					else if(addedTags.get(key).contains("import"))
					{
						add_imports++;
					}
//					System.out.println("Tag " + added.get(key) + " " + key + " added.");
					numAdded++;
				}
			}
		}
	}
	
/*	public void listRelocates(){
		HashMap<String, String> elements1 = new HashMap<>();
		HashMap<String, String> elements2 = new HashMap<>();
		
		Diff myDiff = DiffBuilder.compare(Input.fromString(baseSchema.substring(baseSchema.indexOf("<?xml"))))
	              .withTest(Input.fromString(testSchema.substring(testSchema.indexOf("<?xml"))))
	              .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
	              .ignoreComments()
	              .ignoreWhitespace()
	              .checkForSimilar()
	              .build();	
									
		Iterable<Difference> diffs = myDiff.getDifferences();
		if(!myDiff.hasDifferences())
		{
			System.out.println("No differences found");
		}
		else
		{
			for (Difference difference : diffs) {
				String nodeNameTest = "";
				String nodeTypeTest = "";
				String nodeNameControl = "";
				String nodeTypeControl = "";
				short nodeType = -1;
				
				try
				{
					nodeType = difference.getComparison().getTestDetails().getTarget().getNodeType();
					nodeTypeTest = difference.getComparison().getTestDetails().getTarget().getNodeName();
					nodeTypeControl = difference.getComparison().getControlDetails().getTarget().getNodeName();
					if(nodeType == Node.ATTRIBUTE_NODE)
					{
					}
					if(nodeType == Node.ELEMENT_NODE)
					{
						if(nodeTypeTest.contains("element") || nodeTypeTest.contains("attribute"))
						{
							nodeNameTest = 
							  nodeTypeTest
							+ " "
							+ difference.getComparison().getTestDetails().getTarget().getAttributes().getNamedItem("name").toString();
							
							nodeNameControl = 
							  nodeTypeControl
							+ " "
							+ difference.getComparison().getControlDetails().getTarget().getAttributes().getNamedItem("name").toString();
						}
					}
				}
				catch(NullPointerException e)
				{

				}
				if(!nodeNameControl.isEmpty() && !nodeNameTest.isEmpty())
				{
					elements1.put(difference.getComparison().getTestDetails().getTarget().getAttributes().getNamedItem("name").toString(), nodeTypeTest);
					elements2.put(difference.getComparison().getControlDetails().getTarget().getAttributes().getNamedItem("name").toString(), nodeTypeControl);
				}
			}
		}
		
		for(String key : elements1.keySet())
		{
			for(String obj : elements2.keySet())
			{
				if(key.equals(obj) && !key.isEmpty())
				{
					if(elements1.get(key).contains("element"))
					{
						rel_elements++;
					}
					else if(elements1.get(key).contains("attribute"))
					{
						rel_attributes++;
					}
					System.out.println("Tag named \"" + key + "\" is just realocated inside schema.");
					numRelocated++;
					break;
				}
			}
		}
	}
*/
	public void setCounters() throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        
        Document docBase = docBuilder.parse(new ByteArrayInputStream(baseSchema.getBytes()));
        docBase.getDocumentElement().normalize();
        Document docTest = docBuilder.parse(new ByteArrayInputStream(testSchema.getBytes()));
        docTest.getDocumentElement().normalize();
        
        before_elements = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","element").getLength();
        before_attributes = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","attribute").getLength();
        before_imports = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","import").getLength();
        before_complextypes = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","complexType").getLength();

        after_elements = docTest.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","element").getLength();
        after_attributes = docTest.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","attribute").getLength();
        after_imports = docTest.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","import").getLength();
        after_complextypes = docTest.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","complexType").getLength();
	}
	public void setCountersBefore() throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        
        Document docBase = docBuilder.parse(new ByteArrayInputStream(uniqueSchema.getBytes()));
        
        before_elements = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","element").getLength();
        before_attributes = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","attribute").getLength();
        before_imports = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","import").getLength();
        before_complextypes = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","complexType").getLength();

    	rem_elements = before_elements;
    	rem_attributes = before_attributes;
    	rem_imports = before_imports;
    	rem_complextypes = before_complextypes;
	}
	public void setCountersAfter() throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        
        Document docBase = docBuilder.parse(new ByteArrayInputStream(uniqueSchema.getBytes()));
        
        after_elements = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","element").getLength();
        after_attributes = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","attribute").getLength();
        after_imports = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","import").getLength();
        after_complextypes = docBase.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","complexType").getLength();

    	add_elements = after_elements;
    	add_attributes = after_attributes;
    	add_imports = after_imports;
    	add_complextypes = after_complextypes;
	}
	public Integer getNumAdded(){
		return numAdded;
	}
	
	public Integer getNumRemoved(){
		return numRemoved;
	}
	
	public Integer getNumRelocated(){
		return numRelocated;
	}
	
	public Integer getNumRefactored(){
		return numRefactored;
	}
	public Integer getBeforeElements(){
		return before_elements;
	}
	public Integer getBeforeAttributes(){
		return before_attributes;
	}
	public Integer getBeforeImports(){
		return before_imports;
	}
	public Integer getBeforeComplexTypes(){
		return before_complextypes;
	}
	public Integer getAfterElements(){
		return after_elements;
	}
	public Integer getAfterAttributes(){
		return after_attributes;
	}
	public Integer getAfterImports(){
		return after_imports;
	}
	public Integer getAfterComplexTypes(){
		return after_complextypes;
	}
	public void resetCounters(){
		add_elements = 0;
		add_attributes = 0;
		add_imports = 0;
		add_complextypes = 0;
		rem_elements = 0;
		rem_attributes = 0;
		rem_imports = 0;
		rem_complextypes = 0;
		ref_elements = 0;
		ref_attributes = 0;
		ref_imports = 0;
		rel_elements = 0;
		rel_attributes = 0;
		before_elements = 0;
		before_attributes = 0;
		before_imports = 0;
		before_complextypes = 0;
		after_elements = 0;
		after_attributes = 0;
		after_imports = 0;
		after_complextypes = 0;
	}
	
}