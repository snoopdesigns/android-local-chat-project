package com.home.simplechatapplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class Configuration {
	final static String TAG = "xSI_PreferencesHandler";
	private String m_filename = null;
	private Map<String, ClientConfigurationSection> m_ClientUIConfiguration = new Hashtable<String, ClientConfigurationSection>();
	Configuration(String m_filename) 
	{
		Log.v(TAG, "initConfiguration()");
	    this.m_filename = m_filename;
	    Document doc = null;
    	try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(new File(this.m_filename));

			NodeList sections = doc.getElementsByTagName("section");
			for (int i=0; i<sections.getLength(); i++) {
				NamedNodeMap sec_attr = sections.item(i).getAttributes();
				ClientConfigurationSection confSection = new ClientConfigurationSection();
				String name = null;
				String desc = null;
				if (sec_attr != null) {
					name = sec_attr.getNamedItem("name").getNodeValue();
					desc = sec_attr.getNamedItem("desc").getNodeValue();
					confSection.setName(name);
					confSection.setDesc(desc);
				}
				
				NodeList params = sections.item(i).getChildNodes();
				for (int j=0; j<params.getLength(); j++) {
					if (params.item(j).getNodeName().equals("param")) {
						NamedNodeMap par_attr = params.item(j).getAttributes();
						if (par_attr != null) {
							String key = par_attr.getNamedItem("key").getNodeValue();
							String value = par_attr.getNamedItem("value").getNodeValue();
							confSection.getDictionary().put(key, value);
						}
					}
				}
	            this.m_ClientUIConfiguration.put(name, confSection);
			}
    	} catch (ParserConfigurationException pce) {
    		Log.e (TAG, "ParserConfigurationException:"+pce.getMessage());
    	} catch (IOException ioe) {
    		Log.e (TAG, "IOException:"+ioe.getMessage());
    	} catch (SAXException se) {
    		Log.e (TAG, "SAXException:"+se.getMessage());
    	}
    }
	
	public String getFileName()
	{
		return this.m_filename;
	}
	public String getHostname()
	{
		return this.getValueFromSection("UIConfiguration", "server_ip", "192.168.0.1");
	}
	public void write(String filename_arg) {
        String filename = null;
        if (filename_arg == null) {
            filename = this.m_filename;
        }
        else {
            filename = filename_arg;
        }
        Log.v(TAG, "Write xml file "+filename);
    	File newxmlfile = new File (filename);
    	try{
            newxmlfile.createNewFile();
        }catch(IOException e) {
            Log.e(TAG, "IOException in create new File:"+e.getMessage());
        }
        FileOutputStream fileos = null;
        try{
            fileos = new FileOutputStream(newxmlfile);
        }catch(FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException:"+e.getMessage());
        }
        
        XmlSerializer serializer = Xml.newSerializer();
        try{
	        serializer.setOutput(fileos, "utf-8");
	        serializer.startDocument(null, Boolean.valueOf(true));
	        serializer.startTag(null, "Configuration");
	        
	        Set<String> names = this.m_ClientUIConfiguration.keySet();
	        for (String name : names) {
	        	ClientConfigurationSection section = this.m_ClientUIConfiguration.get(name);
	        	serializer.startTag(null, "section");
	        	serializer.attribute(null, "name", section.getName());
	        	serializer.attribute(null, "desc", section.getDesc());
	            Set<String> keys = section.getDictionary().keySet();
	            for (String string : keys) {
	            	serializer.startTag(null, "param");
	            	serializer.attribute(null, "key", string);
	            	serializer.attribute(null, "value", section.getDictionary().get(string));
	            	serializer.endTag(null, "param");
	            }
	        	serializer.endTag(null, "section");
	        }
	        
	        serializer.endTag(null,"Configuration");
	        serializer.endDocument();
	        serializer.flush();
	        fileos.close();
        }catch(Exception e)
        {
            Log.e("Exception","Exception occured in wroting");
            e.printStackTrace();
        }
    }
	public void WriteNodeValue(String section, String name, String value) {
    	Log.v(TAG, "WriteNodeValue("+section+","+name+","+value+")");
    	Document doc = null;
    	try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(new File(m_filename));
			NodeList sections = doc.getElementsByTagName("section");
			for (int i=0; i<sections.getLength(); i++) {
				NamedNodeMap sec_attr = sections.item(i).getAttributes();
				if (sec_attr != null && sec_attr.getNamedItem("name").getNodeValue().equals(section)) {
					Log.v (TAG, "Target section is found");
					NodeList params = sections.item(i).getChildNodes();
					for (int j=0; j<params.getLength(); j++) {
						NamedNodeMap par_attr = params.item(j).getAttributes();
						if (par_attr != null &&
							par_attr.getNamedItem("key").getNodeValue().equals(name)) {
							Log.v(TAG, "Target param is found");
							par_attr.getNamedItem("value").setNodeValue(value);
							break;
						}
					}
					break;
				}
			}
    	} catch (ParserConfigurationException pce) {
    		Log.e (TAG, "ParserConfigurationException:"+pce.getMessage());
    	} catch (IOException ioe) {
    		Log.e (TAG, "IOException:"+ioe.getMessage());
    	} catch (SAXException se) {
    		Log.e (TAG, "SAXException:"+se.getMessage());
    	}
    	try {
    		Source source = new DOMSource(doc);
    		File file = new File(m_filename);
    		Result result = new StreamResult(file);
    		Transformer xformer = TransformerFactory.newInstance().newTransformer();
    		xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			Log.e (TAG, "TransformerConfigurationException:"+e.getMessage());
		} catch (TransformerException e) {
			Log.e (TAG, "TransformerException:"+e.getMessage());
		}
    }
	
	public String getValueFromSection(String name, String key, String defaultValue) {
        ClientConfigurationSection section = this.m_ClientUIConfiguration.get(name);
        if (section != null) {
        	String retName = section.getDictionary().get(key);
        	if (retName != null) {
        		return retName;
        	}
        }
        else {
			section = new ClientConfigurationSection();
			
			String desc = "";
			section.setName(name);
			section.setDesc(desc);
        	
        	this.m_ClientUIConfiguration.put(name, section);
        }
		section.getDictionary().put(key, defaultValue);
		return defaultValue;
	}
	public void setValueFromSection(String name, String key, String value) {
		Log.v(TAG, "setValueFromSection(" + name + "," + key + "," + value + ")");
		ClientConfigurationSection section = this.m_ClientUIConfiguration.get(name);
		section.getDictionary().remove(key);
		section.getDictionary().put(key, value);
	}
	public class ClientConfigurationSection {
	    private String name;
	    private String desc;
	    private Map<String, String> m_myDictionary = new Hashtable<String,String>();

	    public Map<String, String> getDictionary() {
	        return m_myDictionary;
	    }
	    
	    public void setDictionary (Map<String, String> value) {
	    	m_myDictionary = value;
	    }
	  
	    public String getDesc() {
	        return desc;
	    }
	    
	    public void setDesc (String value) { 
	    	desc = value;
	    }

	    public String getName () {
	        return name;
	    }
	        
	    public void setName (String value) {
	    	name = value;
	    }
	}
}
