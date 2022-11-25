package Jena03.jena03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.util.FileManager; //для вывода онтологии из файла
import org.apache.jena.util.iterator.ExtendedIterator; //для итератора, чтобы обойти все классы
import java.io.*; //для ввода/вывода
import org.apache.jena.rdf.model.*;  //всегда подключаем для создания онтологической модели
import org.apache.jena.ontology.*;  //всегда подключаем для создания онтологической модели
import org.apache.jena.shared.JenaException;  //для обработки внештатных ситуаций


public class Jena_cars extends Object {  //метод run произошёл от Object
    //@SuppressWarnings(value = "unused"); аннотация, чтобы убрать некоторые предупреждающие сообщения
    static Logger log = LoggerFactory.getLogger(Jena_cars.class);  //для обработки внештатных ситуаций
    static String ontoFile = "src/Jena03/resources/mfc.owl";  //файл, откуда получаем онтологию
    public static void main(String[] args) {
        new Jena_cars().run();
    }
    public void run() {  //создание онтологической модели
        OntDocumentManager mgr = new OntDocumentManager();          //создание менеджера документов
        OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_MEM);     //объект связанный со спецификациями онтологической модели
        s.setDocumentManager(mgr);                               //"подцепим" онтомодел и менеджер спецификациями
        OntModel ontoModel = ModelFactory.createOntologyModel(s);  //онтологическая модель
        try {  //загрузка файла с онтологией
            InputStream in = FileManager.get().open(ontoFile);  //закачиваем из файла нашу модель
            try {  //построение онтологической модели
                ontoModel.read(in, null);
                ExtendedIterator<OntClass> classes = ontoModel.listClasses();  //для прохода по классам итератор
                StringBuilder data = new StringBuilder();
                while (classes.hasNext()) {
                    OntClass theClass = (OntClass) classes.next();  //берём класс
                    String className = theClass.getLocalName();  //берём его имя
                    if (className != null) {
                        data.append("Class: ").append(className).append("\n");
                        if (theClass.listInstances() != null) {
                            ExtendedIterator<? extends OntResource> insts = theClass.listInstances();  //для прохода по классам итератор
                            data.append(" Inst: [");
                            while (insts.hasNext()) {
                                OntResource theInst = (OntResource) insts.next();
                                data.append("\n  ").append(theInst.getLocalName());
                            }
                            data.append("  \n]\n");
                        }
                    }
                }
                ExtendedIterator<OntProperty> property = ontoModel.listAllOntProperties();  //для прохода по классам итератор
                while (property.hasNext()) {
                    OntProperty prop = (OntProperty) property.next();  //берём класс
                    data.append(prop.getLocalName()).append("\n");
                }
                data.append("\n");

                StringBuilder dataSet = new StringBuilder();
                dataSet.append("@prefix : <http://tutorialacademy.com/2015/jena#> .\n");
                StmtIterator it = ontoModel.listStatements();
                while ( it.hasNext() )
                {
                    Statement stmt = it.nextStatement();

                    Resource subject = stmt.getSubject();
                    Property predicate = stmt.getPredicate();
                    RDFNode object = stmt.getObject();
                    if (!predicate.getLocalName().equals("type") && !predicate.getLocalName().equals("subClassOf")) {
                        System.out.println( subject.getLocalName() + " " + predicate.getLocalName() + " " + object.asResource().getLocalName() );
                        dataSet.append(":").append(subject.getLocalName()).append(" :").
                                append(predicate.getLocalName()).append(" :").append(object.asResource().getLocalName()).append(" .\n");
                    }

                }
                FileOutputStream outDS = new FileOutputStream("src/Jena03/resources/example_road/dataset.n3");
                outDS.write(dataSet.toString().getBytes());
                outDS.close();

                FileOutputStream out = new FileOutputStream("src/Jena03/resources/testFile2.txt");
                out.write(data.toString().getBytes());
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("Ontology" + ontoFile + "loaded.");
        } catch (JenaException je) {
            System.err.println("ERROR" + je.getMessage());
            je.printStackTrace();
            System.exit(0);
        }
    }
}
