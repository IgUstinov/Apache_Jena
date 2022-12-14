package Jena03.jena03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.util.FileManager;                                                                                //для вывода онтологии из файла
import org.apache.jena.util.iterator.ExtendedIterator;                                                                  //для итератора, чтобы обойти все классы
import java.io.*;                                                                                                       //для ввода/вывода
import java.util.ArrayList;

import org.apache.jena.rdf.model.*;                                                                                     //всегда подключаем для создания онтологической модели
import org.apache.jena.ontology.*;                                                                                      //всегда подключаем для создания онтологической модели
import org.apache.jena.shared.JenaException;                                                                            //для обработки внештатных ситуаций

public class Jena_cars {                                                                                                //метод run произошёл от Object
    static Logger log = LoggerFactory.getLogger(Jena_cars.class);                                                       //для обработки внештатных ситуаций
    static String ontoFile = "src/Jena03/resources/TTICarOnto.owl.xml";                                                 //4 японских онтологии
    static String ontoFileControl = "src/Jena03/resources/TTIControlOnto.owl.xml";
    static String ontoFileMap = "src/Jena03/resources/TTIMapOnto.owl.xml";
    static String ontoFileTempaku = "src/Jena03/resources/TTITempakuMapData.owl.xml";
    static String ontoSign = "src/Jena03/resources/roadSign4.owl.xml";                                                  //онтология по знакам
    public static void main(String[] args) {
        new Jena_cars().run();
    }
    public void run() {                                                                                                 //создание онтологической модели
        try {                                                                                                           //загрузка файла с онтологией
            try {                                                                                                       //построение онтологической модели
                OntDocumentManager mgr = new OntDocumentManager();                                                      //создание менеджера документов
                OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_MEM);                                                //объект связанный со спецификациями онтологической модели
                s.setDocumentManager(mgr);                                                                              //"подцепим" онтомодел и менеджер спецификациями
                OntModel ontoModel = ModelFactory.createOntologyModel(s);                                               //онтологическая модель
                InputStream inCar = FileManager.get().open(ontoFile);                                                   //считываем японские онтологии
                InputStream inControl = FileManager.get().open(ontoFileControl);
                InputStream inMap = FileManager.get().open(ontoFileMap);
                InputStream inTempaku = FileManager.get().open(ontoFileTempaku);
                ontoModel.read(inCar, null);                                                                       //добавляем онтологии к модели
                ontoModel.read(inControl, null);
                ontoModel.read(inMap, null);
                ontoModel.read(inTempaku, null);
                ExtendedIterator<OntClass> classes = ontoModel.listClasses();                                           //итератор для прохода по классам (дальше аналогично)
                StringBuilder data = new StringBuilder();                                                               //для записи в файл
                while (classes.hasNext()) {
                    OntClass theClass = classes.next();                                                                 //берём класс
                    String className = theClass.getLocalName();                                                         //берём его имя
                    if (className != null) {
                        data.append("Class: ").append(className).append("\n");                                          //Записываем все классы и его экземпляры в файл
                        if (theClass.listInstances() != null) {
                            ExtendedIterator<? extends OntResource> insts = theClass.listInstances();                   //для прохода по экземплярам класса
                            data.append(" Inst: [");                                                                    //Всё это в файл testFile2.txt
                            while (insts.hasNext()) {
                                OntResource theInst = insts.next();
                                insts.remove();                                                                         //№1 Способ удаления всех экземпляров
                                //data.append("\n  ").append(theInst.getLocalName());
                            }
                            /*while (insts.hasNext()) {                                                                 //для проверки удаления - testFile2.txt
                                OntResource theInst = (OntResource) insts.next();
                                data.append("\n  ").append(theInst.getLocalName());
                            }*/
                            data.append("  \n]\n");
                        }
                    }
                }
                data.append("\n");
                /*.out.println("_______________________________");                                                      //№2 Способ удаления всех экземпляров
                ExtendedIterator<? extends OntResource> ind = ontoModel.listIndividuals();
                while (ind.hasNext()) {
                    OntResource theInd = (OntResource) ind.next();
                    //ind.remove();
                }
                /*while (ind.hasNext()) {                                                                               //проверка удаления
                    OntResource theInd = (OntResource) ind.next();
                    System.out.println((theInd.getLocalName()));
                }
                System.out.println("_______________________________");*/

                /*ontoModel.getResource("#myLane").                                                                     //добавляет триплет, но выдаёт ошибку при записи в rdf ниже
                addProperty(ontoModel.getProperty("#nextPathSegment"), ontoModel.getResource("#otherLane"));*/

                StringBuilder dataSet = new StringBuilder();                                                            //для записи триплетов
                dataSet.append("@prefix : <http://tutorialacademy.com/2015/jena#> .\n");                                //Обязательно нужен prefix в начале
                StmtIterator it = ontoModel.listStatements();
                while (it.hasNext()) {                                                                                  //Удаление всех триплетов японской онтологии
                    Statement stmt = it.nextStatement();
                    Resource subject = stmt.getSubject();
                    Property predicate = stmt.getPredicate();
                    RDFNode object = stmt.getObject();
                    //условия на отбор именно триплетов с экземплярами
                    if (!predicate.getLocalName().equals("type")
                            && !predicate.getLocalName().equals("subPropertyOf")
                            && !predicate.getLocalName().equals("range")
                            && !predicate.getLocalName().equals("domain")
                            && !predicate.getLocalName().equals("subClassOf")
                            && object.isResource()
                            && !predicate.getLocalName().equals("versionIRI")) {
                        it.remove();
                    }
                    /*if (object.isLiteral()) {                                                                         //<Название на английском> label <Название на русском>
                        System.out.println( subject.getLocalName() + " "
                                + predicate.getLocalName() + " "
                                + object.asLiteral() );
                    }*/
                }
                InputStream inSign = FileManager.get().open(ontoSign);                                                  //Добавление онтологии знаков
                ontoModel.read(inSign, null);
                /*if (theClass.getLocalName().equals("OneWayLane")) {
                            theClass.createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#otherLane");
                            theClass.createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#myLane");
                            ontoModel.add(
                                    ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#myLane"),
                                    ontoModel.getProperty("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#nextPathSegment"),
                                    ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#otherLane"));
                        }*/
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#OneWayLane")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Polytechnic"); //Улица политехническая
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Car#MyCar")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Ego");   //Наша машина
                ontoModel.add(
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Ego"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Car#isRunningOn"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Polytechnic")); //Едем по политехнической
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#TrafficSignal")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicTrafficLight1"); //Светофор у магнита
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#TrafficSignal")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicTrafficLight2"); //Светофор на перекрестке
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Control#GreenGo")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Green"); //Зеленый сигнал светофора
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Control#RedStop")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Red");   //Красный сигнал светофора
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#Intersection")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicIntersection");
                ontoModel.getOntClass("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#CrosswalkRoadSegment")
                        .createIndividual("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicCrosswalk");
                ontoModel.add(
                        ontoModel.getResource("http://www.semanticweb.org/алина/ontologies/2022/10/roadSign4#MaximumSpeedLimit"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#isOn"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Polytechnic"));
                /*ontoModel.add(
                        ontoModel.getResource("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#SpeedLimit40"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#isOn"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Polytechnic"));*/
                ontoModel.add(
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Polytechnic"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#hasIntersection"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicIntersection"));
                ontoModel.add(                                                                                          //светофор на перекрестке
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicTrafficLight2"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#isOn"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicIntersection"));
                ontoModel.add(                                                                                          //светофор на дороге (у магнита)
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicTrafficLight1"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#isOn"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Polytechnic"));
                ontoModel.add(
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicTrafficLight1"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#relatedTrafficLight"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Green"));
                ontoModel.add(
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#PolytechnicTrafficLight2"),
                        ontoModel.getProperty("http://www.toyota-ti.ac.jp/Lab/Denshi/COIN/Map#relatedTrafficLight"),
                        ontoModel.getResource("http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#Red"));
                it = ontoModel.listStatements();
                while (it.hasNext()) {                                                                                  //вывод для проверки + вывод в dataset.n3
                    Statement stmt = it.nextStatement();                                                                //Всё это в файл dataset.n3
                    Resource subject = stmt.getSubject();
                    Property predicate = stmt.getPredicate();
                    RDFNode object = stmt.getObject();
                    if (/*!predicate.getLocalName().equals("type")
                            &&*/ !predicate.getLocalName().equals("subPropertyOf")
                            && !predicate.getLocalName().equals("range")
                            && !predicate.getLocalName().equals("domain")
                            && !predicate.getLocalName().equals("subClassOf")
                            && !predicate.getLocalName().equals("hasTextColor")
                            && !predicate.getLocalName().equals("hasImageColor")
                            && !predicate.getLocalName().equals("hasShape")
                            && !predicate.getLocalName().equals("hasText")
                            && !predicate.getLocalName().equals("hasBackgroundColor")
                            && !predicate.getLocalName().equals("hasFrameColor")
                            && !predicate.getLocalName().equals("hasImage")
                            && object.isResource()
                            && !predicate.getLocalName().equals("versionIRI")
                            && subject.getLocalName() != null
                            && object.asResource().getLocalName() != null
                            && ((subject.getLocalName().equals("Ego"))
                            || (subject.getLocalName().equals("Polytechnic"))
                            || (subject.getLocalName().equals("PolytechnicTrafficLight1"))
                            || (subject.getLocalName().equals("PolytechnicTrafficLight2"))
                            || (subject.getLocalName().equals("Green"))
                            || (subject.getLocalName().equals("Red"))
                            || (subject.getLocalName().equals("PolytechnicIntersection"))
                            || (subject.getLocalName().equals("MaximumSpeedLimit"))
                            || (subject.getLocalName().equals("PolytechnicCrosswalk"))
                            || (subject.getLocalName().equals("RedStop"))
                            || (subject.getLocalName().equals("CrosswalkRoadSegment"))
                            || (subject.getLocalName().equals("SpeedLimit40")))
                      ) {
                        System.out.println( subject.getLocalName() + " "                                                //вывод для наглядности
                                + predicate.getLocalName() + " "
                                + object.asResource().getLocalName() );
                        dataSet.append(":").append(subject.getLocalName()).
                                append(" :").append(predicate.getLocalName()).
                                append(" :").append(object.asResource().getLocalName()).append(" .\n");
                    }
                }
                FileOutputStream outF = new FileOutputStream("src/Jena03/resources/ResultTrafficOnto.owl");       //Собранная онтология
                ontoModel.write(outF);
                outF.close();
                FileOutputStream outDS = new FileOutputStream("src/Jena03/resources/example_road/dataset.n3");    //триплеты онтологии
                outDS.write(dataSet.toString().getBytes());
                outDS.close();

                FileOutputStream out = new FileOutputStream("src/Jena03/resources/InstOfClasses.txt");            //Классы, экземпляры, сущности
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
