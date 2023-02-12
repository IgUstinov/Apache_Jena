package Jena03.jena03;

import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import java.io.FileOutputStream;


public class JenaReasoningWithRules {
    public static void main(String[] args)
    {
        Model model = ModelFactory.createDefaultModel();                                                                //создаём модель
        model.read("src/Jena03/resources/example_road/dataset.n3");                                                 //путь к файлу с данными
        Reasoner reasoner = new GenericRuleReasoner(
                Rule.rulesFromURL("src/Jena03/resources/example_road/rules.txt"));                                  //создаём резонер. Путь к файлу правил
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);                                               //создание модель с правилами

        StringBuilder dataSet = new StringBuilder();                                                                    //Запись новых данных в файл
        dataSet.append("@prefix : <http://www.semanticweb.org/igor/ontologies/2022/10/map-ontology#> .\n");                                        //Обязательно нужен prefix в начале
        StmtIterator it = infModel.listStatements();
        while ( it.hasNext() ) {
            Statement stmt = it.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();
            try {
                System.out.println(subject.getLocalName() + " "
                        + predicate.getLocalName() + " "
                        + object.asResource().getLocalName());
                dataSet.append(":").append(subject.getLocalName()).
                        append(" :").append(predicate.getLocalName()).
                        append(" :").append(object.asResource().getLocalName()).append(" .\n");
            } catch (ResourceRequiredException rre) {
                System.out.println(subject.getLocalName() + " "
                        + predicate.getLocalName() + " "
                        + object);
                dataSet.append(":").append(subject.getLocalName()).
                        append(" :").append(predicate.getLocalName()).
                        append(" :").append(object).append(" .\n");
            }
        }
        try {
            FileOutputStream outDS = new FileOutputStream("src/Jena03/resources/example_road/dataset_new.n3");    //Файл с новыми триплетами
            outDS.write(dataSet.toString().getBytes());
            outDS.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
