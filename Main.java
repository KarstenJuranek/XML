package Main;

import org.jdom2.*;
import org.jdom2.Attribute;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.SAXOutputter;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

// Neben dem jdom-2.0.6.1.jar muss aus dem dortigen 'lib'-Vereichnis
// auch jaxen-1.2.0.jar und xercesimpl.jar als Library eingebunden sein
public class Main
{
    static String format(Document Doc)
    {
        XMLOutputter Out = new XMLOutputter();
        return Out.outputString(Doc);
    }

    // ### Example ###

    // 0.1) Create examplary XML data
    static String createXML()
    {
        return
            """
            <?xml version="1.0"?>
            <!-- cf. Ullenboom ³2018: 722 "Java SE9 Standard-Bibliothek" -->
            <!DOCTYPE party
            [
                <!ELEMENT party (guest*)>
                <!ATTLIST party date CDATA #REQUIRED>
                <!ELEMENT guest (drink*, state?)>
                <!ATTLIST guest name CDATA #REQUIRED>
                <!ELEMENT drink (#PCDATA)>
                <!ELEMENT state EMPTY>
                <!ATTLIST state single CDATA #IMPLIED sober CDATA #IMPLIED>
            ]>
            <party date="11.11.2222">
                <guest name="Albert Einstein">
                    <drink>wine</drink>
                    <drink>beer</drink>
                    <state single="false" sober="true"/>
                </guest>
                <guest name="Alberta Einstein">
                    <drink>juice</drink>
                    <state single="true" sober="false"/>
                </guest>
                <guest name="Albertli Einstein">
                    <drink>milk</drink>
                </guest>
            </party>
            """;
    }
    // 0.2) Create exemplary XSD data (not used here)
    static String createXSD()
    {
        return
            """
            <?xml version="1.0"?>
            <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            
               <xsd:element name="party" type="partyType" />
            
               <xsd:complexType name="partyType">
                  <xsd:sequence>
                     <xsd:element name="guest" type="guestType" />
                  </xsd:sequence>
                  <xsd:attribute name="date" type="dateType" />
               </xsd:complexType>
            
               <xsd:complexType name="guestType">
                  <xsd:sequence>
                     <xsd:element name="drink" type="xsd:string" />
                     <xsd:element name="state" type="stateType" />
                  </xsd:sequence>
               </xsd:complexType>
            
               <xsd:simpleType name="dateType">
                  <xsd:restriction base="xsd:string">
                     <xsd:pattern value="[0-3][0-9].[0-1][0-9].[0-9]{4}" />
                  </xsd:restriction>
               </xsd:simpleType>
            
               <xsd:complexType name="stateType">
                  <xsd:complexContent>
                     <xsd:restriction base="xsd:anyType">
                        <xsd:attribute name="sober" type="xsd:boolean" />
                        <xsd:attribute name="single" type="xsd:boolean" />
                     </xsd:restriction>
                  </xsd:complexContent>
               </xsd:complexType>
            
            </xsd:schema>
            """;
    }
    // 0.3) Create examplary XSLT data
    static String createXSLT()
    {
        return
            """
            <?xml version="1.0"?>
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            <!-- Cf. Ullenboom ³2018: 795 "Java SE9 Standard-Bibliothek" -->
                <!-- Match root element of XPath tree: output HTML head und body -->
                <xsl:template match="/">
                   <html>
                      <head><title>Let's make a party</title></head>
                      <!-- Branch deeper into XPath tree: output of other templates inserted here -->
                      <body><xsl:apply-templates /></body>
                   </html>
                </xsl:template>

                <!-- A headline is created for the element 'party' in the output. -->
                <!-- The attribute 'date' will be output in the headline. -->
                <xsl:template match="party">
                   <h1>Party table for the <xsl:value-of select="@date" /></h1>
                   <xsl:apply-templates />
                </xsl:template>

                <!-- Output a greeting for each guest. -->
                <xsl:template match="guest">
                   <p><h2>Hello <xsl:value-of select="@name" />!</h2></p>
                   <xsl:apply-templates />
                </xsl:template>

                <!-- Each guest will be offered his/her favourite drink. -->
                <xsl:template match="drink">
                   <p>Here is a <xsl:value-of select="." /> for you.</p>
                </xsl:template>

                <!-- A conditional output is created here: Each guest shows his states. -->
                <xsl:template match="state">
                   <xsl:if test="@sober='true'"><h3>I'm still sober!</h3></xsl:if>
                   <xsl:if test="@single='true'"><h3>You can marry me!</h3></xsl:if>
                   <hr />
                </xsl:template>
            </xsl:stylesheet>
            """;
    }

    // 1) Parse XML plain text to represented/interpreted document
    static Document parse(String XML)
    {
        System.out.println("\n\r=== Parsing ===");

        Document Doc = null;

        try
        {
            SAXBuilder Bldr = new SAXBuilder(XMLReaders.DTDVALIDATING); // *)
            Doc = Bldr.build(new StringReader(XML));
            // *) Here: validating by a DTD;
            //    Alternative: XML schema file (cf. 0.2 above) => very cumbersome to validate!

            DocType Type = Doc.getDocType();
            System.out.println(Type);
            //System.out.println();
        }
        catch (JDOMException | IOException E)
        { System.out.println(E.getMessage()); }

        return Doc;
    }

    // 2) Process document
    static void process(Document Doc)
    {
        System.out.println("\n\r=== Processing ===");

        // Get root element
        Element Root = Doc.getRootElement();
        System.out.println(Root);       // start point for further processing
        System.out.println();

        // Get child elements incl. attributes (guests)
        Element Guest = Root.getChild("guest");     // only 1st guest
        List<Element> Guests = Root.getChildren();  // all guests
        System.out.println(Guest+", "+Guests);
        System.out.println();

        for (Element Guezt : Guests)            // attributes of a guest (name)
        {
            Attribute Attr = Guezt.getAttribute("name");    // entire attribute
            System.out.println(Attr+": "+Attr.getValue());          // value of an attribute
        }
        System.out.println();

        // Child elements of all guests
        for (Element Guezt : Guests)
        {
            List<Element> Drinks = Guezt.getChildren("drink"); // drinks of a guest
            System.out.print(Guezt+": ");
            for (Element Drink : Drinks)                    // get contents of drink tags
                System.out.print(Drink.getText()+"\t");
            System.out.println();

            Element State = Guezt.getChild("state");        // state of a guest (1 tag)
            if (State != null)                              // null if no state tag
                for (Attribute Attr : State.getAttributes())   // read state attributes
                    System.out.println(Attr+": "+Attr.getName()+"="+Attr.getValue());
            //System.out.println();
        }
    }

    // 3) Modify document
    static void modify(Document Doc)
    {
        System.out.println("\n\r=== Modification ===");

        // Get root element
        Element Root = Doc.getRootElement();

        // Change existing element (content/text or attribute)
        Element Guest = Root.getChild("guest");
        Guest.setAttribute("name", "Alberto Einstein");
        Element Drink = Guest.getChild("drink");
        Drink.setText("coke");              // wine => coke
        Element State = Guest.getChild("state");
        State.setAttribute("sober", "false");

        // Add new element with attributes
        List<Element> Guests = Root.getChildren();
        Guest = Guests.get(2);
        State = new Element("state");
        State.setAttributes(List.of(new Attribute("single", "true"),
                                    new Attribute("sober", "false")));
        Guest.addContent(State);

        // Add another guest with a drink
        Element Guezt = new Element("guest");
        Guezt.setAttribute("name", "Albertum Einsteinium");
        Element Drynk = new Element("drink");
        Drynk.setText("tea");
        Guezt.addContent(Drynk);
        Root.addContent(Guezt);

        System.out.println(format(Doc));
    }

    // 4) Access document via paths
    static void access(Document Doc)
    {
        System.out.println("\n\r=== Accessing ===");

        // Access party guest names
        XPathExpression<Object> XPath1 = XPathFactory.instance().compile("/party/guest/@name");
        List<Object> Names = XPath1.evaluate(Doc);   // file system notation
        System.out.println(Names);

        // Access drinks of guests
        XPathExpression<Element> XPath2 = XPathFactory.instance().compile
            ("/party/guest/drink", Filters.element());
        for (Element Elem : XPath2.evaluate(Doc))
            System.out.print(Elem.getValue()+" ");
        System.out.println();

        // ... and many more
    }

    // 5) Transform document via XSLT
    static void transform(Document Doc)
    {
        System.out.println("\n\r=== Transformation ===");

        Source XML = new JDOMSource(Doc);
        JDOMResult HTML = new JDOMResult();

        try
        {
            String XSLT = createXSLT();
            Transformer Trans = TransformerFactory.newInstance().newTransformer
                (new StreamSource(new StringReader(XSLT)));
            Trans.transform(XML, HTML);

            System.out.println(format(HTML.getDocument()));
        }
        catch (TransformerException E)
        { System.out.println(E.getMessage()); }
    }

    // ### Main ###
    public static void main(String[] args)
    {
        Document Doc;

        // Example
        {
            System.out.println("#######");
            System.out.println("Example");
            System.out.println("#######");

            // 0) Create XML example data
            String XML = createXML();

            // 1) Parse XML file
            Doc = parse(XML);

            // 2) Process elements/attributes/data
            process(Doc);

            // 3) Change document
            modify(Doc);

            // 4) Access document by XPath
            access(Doc);

            // 5) Transform document by XSLT
            transform(Doc);
        }

        // Exercise
        {
            // 0) Describe an XML with DTD for storing a (Hash)Map of key-value entries:
            //    * keys with any content and a boolean attribute Unique (as many keys as required)
            //    * values with any content and no attributes
            //    Fill with exemplary data: Pi=3.1415926535, E=2.718281828, and C=299792458
            //    How are optional tags/elements vs attributes defined?
            // 1) Process all entries without XPath and construct/return a String output like
            //    {Pi=3.1415926535 (true), E=2.718281828, C=299792458}
            // 2) Access all keys Pi, E, C using XPath and output like {Pi, E, C};
            //    Access only key attributes using XPath and output like {true}
            //    (Hint: Use a boolean for calling the access method with false for keys
            //     and true for attributes)
            // 3) Transform the document to a HTML representation (without ""):
            //    "
            //      Key-Value Structure:
            //      Entry: [ Pi(true) = 3.1415926535 ]
            //      Entry: [ E = 2.718281828 ]
            //      Entry: [ C = 299792458 ]
            //    "
        }
    }
}
