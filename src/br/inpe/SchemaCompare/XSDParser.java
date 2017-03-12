// PROCURAR 5 PROJETOS DO GITHUB!

package br.inpe.SchemaCompare;

// IMPORTS

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//DOCUMENT LOCATOR (DOM)

// CLASS DEFINITION ----------------------------------------------------------//
public class XSDParser {

    private String filename;
    private Document doc;
    int quantityOfElements = 0,
            quantityOfAttributes = 0,
            quantityOfComplexTypes = 0,
            quantityOfImports = 0;
    int maxDepth = 1;
    private String _XMLNS = "http://www.w3.org/2001/XMLSchema";

    public String getFilename()
    {
        return filename;
    }

    public XSDParser(File fl) throws IOException
    {
        filename = fl.getPath();

        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.parse(fl);
            doc.getDocumentElement().normalize();

            quantityOfElements = doc.getElementsByTagNameNS(_XMLNS,"element").getLength();
            quantityOfAttributes = doc.getElementsByTagNameNS(_XMLNS,"attribute").getLength();
            quantityOfComplexTypes = doc.getElementsByTagNameNS(_XMLNS,"complexType").getLength();
            quantityOfImports = doc.getElementsByTagNameNS(_XMLNS,"import").getLength();
        }
        catch (ParserConfigurationException e)
        {
        }
        catch (IOException ed) 
        {
        }
        catch (org.xml.sax.SAXException e)
        {
        }
    }
    
    public XSDParser(byte[] fl) throws IOException
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            
            doc = docBuilder.parse(new ByteArrayInputStream(fl));
            doc.getDocumentElement().normalize();
            
            quantityOfElements = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","element").getLength();
            quantityOfAttributes = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","attribute").getLength();
            quantityOfComplexTypes = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","complexType").getLength();
            quantityOfImports = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema","import").getLength();
        }
        catch (ParserConfigurationException e)
        {
        }
        catch (IOException ed) 
        {
        }
        catch (org.xml.sax.SAXException e)
        {
        }
    }
	
// DESIRED METRICS:
// 1 - (x) Quantity of elements
// 2 - (x) Quantity of attributes
// 3 - (x) Quantity of complex types.
// 4 - (x) Attributes per element (average)
// 5 - (x) Complex types per element (average)
// 6 - (x) Maximum number of attributes in an element
// 7 - (x) Maximum number of attributes in a complex type
// 8 - (x) Maximum number of complex types in an element
// 9 - (x) Maximum number of sub-elements
// 10 - (x) Maximum depth
	
	// METRIC 1
    public int getQuantityOfElements()
    {
         return quantityOfElements;
    }
    
    // METRIC 2
    public int getQuantityOfAttributes()
    {
    	return quantityOfAttributes;
    }
    
    // METRIC 3
    public int getQuantityOfComplexTypes()
    {
    	return quantityOfComplexTypes;
    }
    
    public int getQuantityOfImports()
    {
    	return quantityOfImports;
    }
    
    // METRIC 4
    public double getAttributeAverage()
    {
    	return (double)this.quantityOfAttributes/this.quantityOfElements;
    }
    
    // METRIC 5
    public double getComplexTypeAverage()
    {
    	return (double)this.quantityOfComplexTypes/this.quantityOfElements;
    }
    
    // METRIC 6
    public int getMaxAttributesInAnElement()
    {
    	int max = 0; 
   	int maxTemp = 0;
    	
    	NodeList list = doc.getElementsByTagNameNS(_XMLNS, "element");
    	for(int a = 0; a < list.getLength(); a++)
    	{
            Node aNode = list.item(a);
            if(aNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element aElem = (Element)aNode;
                NodeList list2 = aElem.getElementsByTagNameNS(_XMLNS, "attribute");

                maxTemp = list2.getLength();

                if(maxTemp > max)
                    max = maxTemp;
            }
    	}
    	
    	return max;
    }

    // METRIC 7
    public int getMaxAttributesInAComplexType()
    {
    	int max = 0;
    	int maxTemp = 0;
    	
    	NodeList list = doc.getElementsByTagNameNS(_XMLNS, "complexType");
    	for(int a = 0; a < list.getLength(); a++)
    	{
            Node aNode = list.item(a);
            if(aNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element aElem = (Element)aNode;
                NodeList list2 = aElem.getElementsByTagNameNS(_XMLNS, "attribute");

                maxTemp = list2.getLength();

                if(maxTemp > max)
                    max = maxTemp;
            }
        }
    	
    	return max;
    }
    
    // METRIC 8
    public int getMaxComplexTypesInAnElement()
    {
    	int max = 0;
    	int maxTemp = 0;
    	
    	NodeList list = doc.getElementsByTagNameNS(_XMLNS, "element");
    	for(int a = 0; a < list.getLength(); a++)
    	{
            Node aNode = list.item(a);
            if(aNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element aElem = (Element)aNode;
                NodeList list2 = aElem.getElementsByTagNameNS(_XMLNS, "complexType");

                maxTemp = list2.getLength();

                if(maxTemp > max)
                    max = maxTemp;
            }
    	}
    	
    	return max;
    }
    
    // METRIC 9
    public int getMaxQuantityOfSubElements()
    {
    	int max = 0;
    	int maxTemp = 0;
    	
    	NodeList list = doc.getElementsByTagNameNS(_XMLNS, "element");
    	for(int a = 0; a < list.getLength(); a++)
    	{
            Node aNode = list.item(a);
            if(aNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element aElem = (Element)aNode;
                NodeList list2 = aElem.getElementsByTagNameNS(_XMLNS, "element");

                maxTemp = list2.getLength();

                if(maxTemp > max)
                    max = maxTemp;
            }
    	}
    	
    	return max;
    }

    // METRIC 10
    public int getMaxDepth()
    {
    	Element aElem = doc.getDocumentElement();
    	int level = 1;
    	NodeList children = aElem.getChildNodes();
    	stepInto(children, level);
    	
    	return maxDepth;
    }
    
	// AUXILIAR METHODS ------------------------------------------------------------------//
	
    private void stepInto(NodeList nl, int lev)
    {
    	lev++;
    	if(nl != null && nl.getLength() > 0)
    	{
            for(int i = 0; i < nl.getLength(); i++)
            {
                Node aNode = nl.item(i);
                if(aNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    stepInto(aNode.getChildNodes(), lev);
                    if(lev > maxDepth)
                        maxDepth = lev;
                }
            }
    	}
    }
}
