package com.eactive.eai.common.generator;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimpleDtoGenerator {
	
	private static boolean isDebug = false;

	private static boolean useAnotation = false;
	
	private static final String[] RESERVED_WORDS = {"default", "class", "public", "void"};
    private final String packageName;
    HashMap<String, XClass> classes = new HashMap<>();
    private String rootTageName;
    private int indentLevel = 0;
    private static String indentStep = "    ";
	
    public SimpleDtoGenerator(String packageName) {
    	this.packageName = packageName;
    }
    
    private static String getIndentText(int indentLevel) {
    	String indentText = "";
    	for (int i = 0; i < indentLevel; i++) {
            indentText += indentStep;
        }
    	return indentText;
    }
    
    private static String cap(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private static String getLastDotInList(String str) {
        if (str.indexOf('.') > -1) {
            String[] sr = str.split("/`./");
            str = "";
            for (int i = 0; i < sr.length; i++) {
                sr[i] = cap(sr[i]);
                str += sr[i];
            }
        } else {
            str = cap(str);
        }
        return str;
    }
	
    private static boolean checkInArray(String[] array, String str) {
    	for(int i=0; i< array.length; i++) {
    		if(str.equals(array[i])) return true;
    	}    	
    	return false;
    }
    
    private static String reservedCheck(String str) {
        if (checkInArray(RESERVED_WORDS, str)) {
            return "_" + str;
        }
        return str;
    }
    
    private static String mkClassName(String name) {
        name = getLastDotInList(name);
        name = name.replace("-", "_");
        name = name.replace("#", "");
        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        while (name.contains("_")) {
            int pos = name.indexOf("_");
            name = name.substring(0, pos) + name.substring(pos + 1, pos + 2).toUpperCase() + name.substring(pos + 2);
        }
        return reservedCheck(name);
    }
    
    private static void writeToFile(String data, String rootTageName) {
    	PrintWriter out = null;
    	try {
            out = new PrintWriter(mkClassName(rootTageName) + ".java");
            out.println(data);            
        } catch (Exception err) {
            err.printStackTrace();
        }
        finally {
        	if(out != null) out.close();
        }
    }
    
    private String getCurrentDateTime() {
    	SimpleDateFormat   sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        return sdf.format(new java.util.Date());
    }
    
    private String generateClassText(XClass cls) {

        String headers = "", root = "", isStatic = "", fields = "", accessors = "", inners = "";
        String tostring = "";
        String comments = "";
        
        if (cls.name.equals(rootTageName)) {
            if (packageName != null) {
                headers = "package " + packageName + ";\n";
            }
            
            comments = 
            		"\n//-----------------------------------------------------------------------------------------------------------\n" +
            		"// Generated by eactive DTO Generator, created  : " + getCurrentDateTime() +"\n" +
            		"//-----------------------------------------------------------------------------------------------------------\n";
            
            headers += "import java.util.List;\n";

            if(useAnotation) {
            	headers += 
	            		"\nimport org.simpleframework.xml.Attribute;\n" +
	                    "import org.simpleframework.xml.Element;\n" +
	                    "import org.simpleframework.xml.Text;\n" +
	                    "import org.simpleframework.xml.ElementList;\n" +
	                    "import org.simpleframework.xml.Root;\n\n" +
	                    "import java.net.URL;\n" ;
            	
            	root = "\n@Root(name=\"" + cls.name + "\")\n";
            }
            

            // inner class
            for (Map.Entry<String, XClass> cl : classes.entrySet()) {
                if (!cl.getValue().name.equals(rootTageName)) {
                    inners += generateClassText(cl.getValue()) + "\n";
                }
            }
        } else {
            indentLevel++;
            root = "\n";
            // static inner class
            isStatic = "static ";
        }

        fields = generateFieldText(cls.fieldNames, cls.fields);
        accessors = generateAccessors(cls.fieldNames, cls.fields);
        
        tostring = generateToString(cls.name, cls.fieldNames, cls.fields);
        String indentText = getIndentText(indentLevel);

        indentLevel--;
        
        return headers + comments + root + 
        		indentText + "public " + isStatic + "class " + mkClassName(cls.name) + " {\n" +
                	fields + "" + 
                	accessors + 
                	inners +
                	tostring +
        		"\n" + indentText + "}";
    }

    private String generateFieldText(ArrayList<String>fieldNames, HashMap<String, XField> fields) {
        String str = "";
        indentLevel++;

        for(int idx=0; idx<fieldNames.size();idx++) {
        	String fName = fieldNames.get(idx);
            XField f = fields.get(fName);

            if (f.name.equals("#text")) {
                f.name = "textValue";
                f.dataType = "String";
                f.isInlineList = false;
                f.isList = false;
            }

            String annotation = "";
            
            if(useAnotation) {
            	annotation = f.isAttribute ? "@Attribute(name=\"" + f.name + "\", required=false)" : "@Element(name=\"" + f.name + "\", required=false)";
            }
            
            boolean isClass = classes.containsKey(f.dataType);
            String dataType = isClass ? mkClassName(f.dataType) : f.dataType;

            if (f.isList || f.isInlineList) {
                dataType = "List<" + dataType + ">";
                if(useAnotation) {
                	annotation = "@ElementList(name=\"" + f.name + "\", required=false" + (f.isInlineList ? ", entry=\"" + f.name + "\", inline=true)" : ")");
                }
            }

            if(useAnotation) {
	            if (f.name.equals("textValue")) {
	                annotation = "@Text(required=false)";
	            }
            }
            
            String indentText = getIndentText(indentLevel);
            if(useAnotation) {
            	str += "\n" + indentText + annotation + "\n" + indentText + dataType + " " + mkFieldName(f.name) + ";\n";
            }
            else {
            	str += "\n" + indentText + dataType + " " + mkFieldName(f.name) + ";\n";
            }
        }
        indentLevel--;
        return str;
    }

    private String generateAccessors(ArrayList<String> fieldNames, HashMap<String, XField> fields) {
        String str = "";
        indentLevel++;

        String indentText = getIndentText(indentLevel);
        
        for(int idx=0; idx<fieldNames.size(); idx++) {
        	String fName = fieldNames.get(idx);
            XField f = fields.get(fName);
            boolean isClass = classes.containsKey(f.dataType);
            String dataType = isClass ? mkClassName(f.dataType) : f.dataType;
            if (f.isList || f.isInlineList) {
                dataType = "List<" + dataType + ">";
            }

            str += "\n" + indentText + "public " + dataType + " get" + cap(mkFieldName(cap(f.name))) + "() {return this." + mkFieldName(f.name) + ";}\n";
            str += indentText + "public void set" + cap(mkFieldName(cap(f.name))) + "(" + dataType + " value) {this." + mkFieldName(f.name) + " = value;}\n";
        }
        indentLevel--;
        return str;
    }
    
    private String generateToString(String className, ArrayList<String> fieldNames, HashMap<String, XField> fields) {
    	
    	if(fieldNames.size() < 1) return "";
    	
        StringBuffer sb = new StringBuffer();
        indentLevel++;

        String indentText = getIndentText(indentLevel);
        
        sb.append("\n");
        sb.append(indentText);
        sb.append("@Override\n");
        sb.append(indentText);
        sb.append("public String toString() {\n");
    
        for(int idx=0; idx<fieldNames.size(); idx++) {
        	String fName = fieldNames.get(idx);
            if(idx == 0) {
            	sb.append(indentText + indentStep);
            	sb.append( String.format("return \"%s[", className) );
            }
            else {
            	sb.append(indentText + indentStep);
            	sb.append("+ \", ");
            }
            sb.append( String.format("%s=\" + %s \n", fName, fName) );
        }
        sb.append(indentText + indentStep);
        sb.append("+ \"]\";");
        
        sb.append("\n" + indentText);
        sb.append("}");
    
        
        indentLevel--;
        return sb.toString();
    }
    
    private String mkFieldName(String name) {
        name = getLastDotInList(name);
        name = name.replace("-", "_");
        name = name.replace("#", "");
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        while (name.contains("_")) {
            int pos = name.indexOf("_");
            name = name.substring(0, pos) + name.substring(pos + 1, pos + 2).toUpperCase() + name.substring(pos + 2);
        }
        return reservedCheck(name);
    }
    
    public void addClass(String name) {
    	if (!classes.containsKey(name)) {
            XClass cla = new XClass();
            cla.name = name;
            classes.put(name, cla);
            if(isDebug) System.out.println("\tAdding class:\t" + name);
        }
    }
    
    public void addField(String className, String fieldName, String dataType) {
    	addField(className, fieldName, dataType, false);
    }
    
    public void addField(String className, String fieldName, String dataType, boolean isInlineList) {
    	XClass cla = classes.get(className);
    	if (!cla.fields.containsKey(fieldName)) {
            XField xf = new XField();
            xf.name = fieldName;
            xf.isInlineList = isInlineList;
            xf.isAttribute = true;
            if(isDebug) System.out.println("\t\t\tAdding attribute field: " + xf.name + " to " + cla.name.toString());
            cla.fields.put(fieldName, xf);
        }
    	XField xf = cla.fields.get(fieldName);
        xf.dataType = dataType;
        cla.fields.put(fieldName, xf);
        cla.fieldNames.add(fieldName);
    }
    
    public void addGroup(String className, String fieldName) {
    	addField(className, fieldName, fieldName, true);
    	addClass(fieldName);
    }
    
    public String getClassText(String className) {
    	return generateClassText(classes.get(className));
    }
    
    public void setRootTagName(String rootName) {
    	this.rootTageName = rootName;
    }
    
    public static void main(String[] argv) {
    	SimpleDtoGenerator gen = new SimpleDtoGenerator("com.eactive.eai.dto");
    	
    	//-------------------------------------------------------    	
    	// TODO : 
    	// 1. check dataType mapping rule
    	// 2. add field comment with rule data (name, type, length, korean name)
    	//-------------------------------------------------------
    	// # Sample

    	// Root Node
    	gen.setRootTagName("person");
    	
    	// Root Fields 
    	gen.addClass("person");
    	gen.addField("person", "name", "String");
    	gen.addField("person", "age", "int");
    	gen.addField("person", "male", "boolean");
    	
    	// Group Add
    	gen.addGroup("person", "address");    	
    	// Group Fields Add
    	gen.addField("address", "addr", "String");
    	gen.addField("address", "zipcode", "String");
    	
    	gen.addField("person", "state", "boolean");
    	
    	System.out.println(gen.getClassText("person"));
    }
}
