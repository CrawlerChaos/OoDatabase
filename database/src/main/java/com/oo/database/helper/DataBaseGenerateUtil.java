package com.oo.database.helper;


import com.oo.database.BuildConfig;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by OnePiece on 16/3/18.
 * DataBaseGenerateUtil
 */
public class DataBaseGenerateUtil {

    private static final String TAG_DATABASE = "Database";
    private static final String TAG_TABLE = "Table";
    private static final String TAG_ID = "id";
    private static final String TAG_COLOUMN = "coloumn";

    private static final String PARAM_GENERATE_ABLE = "generateAble";
    private static final String PARAM_TABLE_NAME = "tableName";
    private static final String PARAM_COLOUMN_TYPE = "type";
    private static final String PARAM_COLOUMN_NULL_ABLE = "nullAble";
    private static final String PARAM_AUTO_INCREMENT = "autoIncrement";

    /*coloumn type*/
    private static final String COLOUMN_TYPE_STRING = "string";
    private static final String COLOUMN_TYPE_INT = "int";
    private static final String COLOUMN_TYPE_LONG = "long";
    private static final String COLOUMN_TYPE_DOUBLE = "double";
    private static final String COLOUMN_TYPE_DATE = "date";

    private static final String OUT_DIR = "database/src/main/java/";
    private static final String PACKAGE = ".entity";

    public static void main(String[] args) throws Exception {


        generateTables(new FileInputStream(new File("database/src/main/res/xml/tables.xml")));

    }


    private static void deleteGenerateFile(){
        File file = new File((OUT_DIR+ BuildConfig.APPLICATION_ID+PACKAGE).replace(".","/"));
        if (file.exists())
            deleteDirectory(file);
    }

    private static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }

    public static void generateTables(InputStream inStream) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = spf.newSAXParser();
            XMLContentHandler handler = new XMLContentHandler();
            saxParser.parse(inStream, handler);
            inStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static class XMLContentHandler extends DefaultHandler {

        private String tagName = null;
        private Entity entity;
        private Schema schema;
        private boolean nullAble;
        private String type = "";

        XMLContentHandler() {
            schema = new Schema(BuildConfig.DATABASE_VERSION, BuildConfig.APPLICATION_ID + PACKAGE);

        }

        @Override
        public void startDocument() throws SAXException {

        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

            if (qName.equals(TAG_TABLE)) {
                if ("false".equals(atts.getValue(PARAM_GENERATE_ABLE)))
                    endElement(namespaceURI, localName, qName);
                else
                    entity = schema.addEntity(atts.getValue(PARAM_TABLE_NAME));
            } else if (qName.equals(TAG_COLOUMN)) {
                type = atts.getValue(PARAM_COLOUMN_TYPE);
                nullAble = !"false".equals(atts.getValue(PARAM_COLOUMN_NULL_ABLE));
            } else if (qName.equals(TAG_ID)) {
                if (entity != null)
                    if ("true".equals(atts.getValue(PARAM_AUTO_INCREMENT)))
                        entity.addIdProperty().autoincrement();
                    else
                        entity.addIdProperty();
            }

            this.tagName = qName;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            if (tagName != null) {
                String content = new String(ch, start, length);
                if (tagName.equals(TAG_COLOUMN) && entity != null) {
                    if (type.equals(COLOUMN_TYPE_STRING)) {
                        if (nullAble)
                            entity.addStringProperty(content);
                        else
                            entity.addStringProperty(content).notNull();
                    } else if (type.equals(COLOUMN_TYPE_INT)) {
                        if (nullAble)
                            entity.addIntProperty(content);
                        else
                            entity.addIntProperty(content).notNull();
                    } else if (type.equals(COLOUMN_TYPE_LONG)) {
                        if (nullAble)
                            entity.addLongProperty(content);
                        else
                            entity.addLongProperty(content).notNull();
                    } else if (type.equals(COLOUMN_TYPE_DOUBLE)) {
                        if (nullAble)
                            entity.addDoubleProperty(content);
                        else
                            entity.addDoubleProperty(content).notNull();
                    } else if (type.equals(COLOUMN_TYPE_DATE)) {
                        if (nullAble)
                            entity.addDateProperty(content);
                        else
                            entity.addDateProperty(content).notNull();
                    } else {
                        throw new IllegalArgumentException("coloumn type illegal!\ntag:"+tagName+" type:"+type);
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {

            if (name.equals(TAG_TABLE)) {
                this.entity = null;
            } else if (name.equals(TAG_DATABASE)) {
                try {
                    deleteGenerateFile();
                    new DaoGenerator().generateAll(schema, OUT_DIR);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (name.equals(TAG_COLOUMN)) {
                type = "";
            }
            this.tagName = null;

        }
    }

}
