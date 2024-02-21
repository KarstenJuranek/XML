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

public class Main
{
    static String format(Document Doc)
    {
        XMLOutputter Out = new XMLOutputter();
        return Out.outputString(Doc);
    }
    // 0.1) Create examplary XML data
    static String createXML()
    {
        /* Verschiedene Versionen bieten andere Zeichenpaletten
        In XML 1.0 sind nur bestimmte Zeichencodierungen wie UTF-8,
        UTF-16 und ISO-8859-1 erlaubt. XML 1.1 ermöglicht eine breitere Palette von
        Zeichencodierungen und erlaubt auch den Gebrauch von Nicht-Unicode-Codierungen.
         */
        return
            """
            <?xml version="1.0" encoding="UTF-8"?>
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

    /*
    Alternative zu DTD der Doctype oben
    Anstelle des DOCTYPES
    <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <!-- Definitionen für party, guest, dateType und stateType -->
    </xsd:schema>
     */
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
        /*
        verschiedene Parser
        Dom Parser      Parsed ein Dokument, indem es das Ganze Dokument lädt(auch den Tree)
        SAX Parser      -"- on event based triggers. Lädt nicht das ganze Doc in den Memory
        JDOM Parser     -"- gleiche wie DOM nur auf einen leichteren Weg
        StAX Parser     -"- effizienter als SAX ansosnten ähnlich
        XPath Parser    -"- ein Dokument based on expressions und anhand der Verwendung von XSLT
        DOM41 Parser    Java lib. XML, XPath, and XSLT using Java Collections Framework
         */
        System.out.println("\n\r=== Parsing ===");
        Document Doc = null;
        try
        {
            //Parsed das XML Doc und speichert das in einer variable ab
            SAXBuilder Bldr = new SAXBuilder(XMLReaders.DTDVALIDATING); // *)
            Doc = Bldr.build(new StringReader(XML));
            // *) Here: validating by a DTD;
            //    Alternative: XML schema file (cf. 0.2 above) => very cumbersome to validate!

            // Inhalte des geparsten Docs werden extrahiert und in Type gespeichert
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
        // Abrufen des Wurzelelements/Startelements (Startpunkt)
        Element Root = Doc.getRootElement();
        System.out.println(Root+"\n");

        //Abrufen nur eines Elements (names des Elements)
        Element Guest = Root.getChild("guest");
        //Abrufen der Liste an gespeicherten Elementen
        List<Element> Guests = Root.getChildren();
        System.out.println(Guest+", "+Guests+"\n");

        //Weitere Modifikation/Ausgabe
        for (Element guest : Guests) {
            Attribute nameAttr = guest.getAttribute("name");
            // >Ausgabe [Attribute: name="Albert Einstein"]: Albert Einstein
            System.out.println(nameAttr + ": " + nameAttr.getValue() + "\n");
        }
        // Child elements of all guests
        for (Element guest : Guests)
        {
            List<Element> Drinks = guest.getChildren("drink"); // drinks of a guest
            // Ausgabe der Guests
            System.out.print(guest+": ");

            // Abrufen der Drinks (Inhalte)
            for (Element Drink : Drinks)
                System.out.println(Drink.getText()+"\t");

            // Abrufen der States single & sober
            Element State = guest.getChild("state");
            if (State != null)   // null if no state tag
                for (Attribute boolean_sober : State.getAttributes())   // read state attributes
                    System.out.println(boolean_sober+": "+boolean_sober.getName()+"="+boolean_sober.getValue());
        } }
    // 3) Modify document
    static void modify(Document Doc)
    {
        System.out.println("\n\r=== Modification ===");
        // Abrufen des Root Elements
        Element Root = Doc.getRootElement();

        // Zugreifen auf den Root (Werte verändern)
        Element Guest = Root.getChild("guest");
        // Namensänderung
        Guest.setAttribute("name", "Alberto Einstein");
        // Zugreifen auf das Kindelement "drink"
        Element Drink = Guest.getChild("drink");
        // Änderung des Textes wine => coke
        Drink.setText("coke");
        // Zugreifen auf ein neues Kindelement
        Element State = Guest.getChild("state");
        // Änderung des boolschen Werts
        State.setAttribute("sober", "false");

        // Add
        //Zugreifen auf den Root
        List<Element> Guests = Root.getChildren();
        // Abrufen des dritten Elements von Guest
        Guest = Guests.get(2);
        // Hinzufügen von neuen Elementen und deren Attribute
        State = new Element("state");
        State.setAttributes(List.of(new Attribute("single", "true"),
                                    new Attribute("sober", "false")));
        Guest.addContent(State);

        // Add another guest with a drink
        // Hinzufügen eines neuen Gastes kein Root notwendig, da nicht spezifisch
        // an eine Stelle gesprungen werden muss
        Element guest = new Element("guest");
        // Deklarieren des neuen Guests und einfügen der Attribute
        guest.setAttribute("name", "Albertum Einsteinium");
        Element drink = new Element("drink");
        drink.setText("tea");
        guest.addContent(drink);
        Root.addContent(guest);
        // Ausgabe mit Methode der modifizierung
        System.out.println(format(Doc));
    }
    // 4) Access document via paths
    static void access(Document Doc)
    {
        System.out.println("\n\r=== Accessing ===");
        // Access party guest names
        // Extrahieren aller Gäste einer Liste
        XPathExpression<Object> XPath1 = XPathFactory.instance().compile("/party/guest/@name");
        // Anwenden der gefundenen Namen
        List<Object> Names = XPath1.evaluate(Doc);   // file system notation
        System.out.println(Names);

        // Access drinks of guests
        XPathExpression<Element> XPath2 = XPathFactory.instance().compile
            ("/party/guest/drink", Filters.element());
        //Andere Elemente wie kommentare oder Textknoten werden ignoriert
        for (Element Elem : XPath2.evaluate(Doc))
            System.out.print(Elem.getValue()+" ");
        System.out.println();
    }
    // 5) Transform document via XSLT
    static void transform(Document Doc) //XSLT anwenden
    {
        System.out.println("\n\r=== Transformation ===");
        // Erstellen des Source Objekts, das transformiert werden soll
        Source XML = new JDOMSource(Doc);
        // Ergebnis, welches das transformierte Doc übernimmt
        JDOMResult HTML = new JDOMResult();
        try
        {
            //Verweisen des erstellten Stylesheets auf eine Variable
            String XSLT = createXSLT();
            // Durchführen der Transformation des Dokuments
            Transformer trans = TransformerFactory.newInstance().newTransformer
                (new StreamSource(new StringReader(XSLT)));
            // applyen der Änderung
            trans.transform(XML, HTML);
            System.out.println(format(HTML.getDocument()));
        }
        catch (TransformerException E)
        { System.out.println(E.getMessage()); }
    }
    // ### Main ###
    public static void main(String[] args)
    {
        Document Doc;
        {
            // 0) Erstellung des XML Docs
            String XML = createXML();
            // 1) Parsing des XML Files
            Doc = parse(XML);
            // 2) Process elements/attributes/data Abrufen der Daten
            process(Doc);
            // 3) Dokument anpassen/ändern/ergänzen
            modify(Doc);
            // 4) Führt verschiedene Zugriffsoperationen aus
            access(Doc);
            // 5) Transformieren des Dokuments mithilfe eines XSLT
            transform(Doc);
        }

        // Exercise
        {
            // 0) Describe an XML with DTD for storing a (Hash)Map of key-value entries:
            /*
            * keys with any c ontent and a boolean attribute Unique (as many keys as required)
            * values with any content and no attributes
            * Fill with exemplary data: Pi=3.1415926535, E=2.718281828, and C=299792458

            <!DOCTYPE hashmap [
                    <!ELEMENT hashmap (entry*)>
                    <!ELEMENT entry (key, value)>
                    <!ATTLIST key Unique (true|false) "true">
                    <!ELEMENT key (#PCDATA)>
                    <!ELEMENT value (#PCDATA)>
            ]>
            <hashmap>
                <entry>
                    <key Unique="true">Pi</key>
                    <value>3.1415926535</value>
                </entry>
                <entry>
                    <key Unique="true">E</key>
                    <value>2.718281828</value>
                </entry>
                <entry>
                    <key Unique="true">C</key>
                    <value>299792458</value>
                </entry>
            </hashmap>

             Wie sind Optional tagt/elements gegen attribute definiert
             * Tags/Elemente werden mithilfe des ? erzeugt. Sie zeigen an, das Elemente
                Null oder einmal im Dokument erscheinen
             * Indem Standardwerte in der Attributdefinition angegeben werden,
                Diese Standardwerte werden als optional gesehen und können weggelassen werden
             */

            // 1) Process all entries without XPath and construct/return a String output like
            //    {Pi=3.1415926535 (true), E=2.718281828, C=299792458}
            /*
                mit getRoot/getChild wie siehe oben
             */
            // 2) Access all keys Pi, E, C using XPath and output like {Pi, E, C};
            //    Access only key attributes using XPath and output like {true}
            //    (Hint: Use a boolean for calling the access method with false for keys
            //     and true for attributes)
            /*
            XPathExpression<Attribute> attributeExpression = XPathFactory.instance().compile(
                        "/hashmap/entry/key/@Unique",
                            Filters.attribute());
            List<Attribute> attributes = attributeExpression.evaluate(document);

            // Print key attributes
            for (Attribute attribute : attributes) {
                System.out.print(attribute.getValue() + ", ");
            }
             */
            // 3) Transform the document to a HTML representation (without ""):
            //    "
            //      Key-Value Structure:
            //      Entry: [ Pi(true) = 3.1415926535 ]
            //      Entry: [ E = 2.718281828 ]
            //      Entry: [ C = 299792458 ]
            //    "
            /*
            gleiche Methode wie oben + ein neues XSLT:
            private static String createXSLT() {

            return "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                  <xsl:template match=\"/\">\n" +
                    <html>\n" +
                      <body>\n" +
                        <p>Key-Value Structure:</p>\n" +
                        <!-- Add your XSLT transformation logic here -->\n" +
                      </body>\n" +
                    </html>\n" +
                  </xsl:template>\n" +
                </xsl:stylesheet>";
    }
             */
        }
    }
}
