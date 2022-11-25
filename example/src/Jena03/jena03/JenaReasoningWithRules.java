package Jena03.jena03;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

import java.io.FileOutputStream;


public class JenaReasoningWithRules {
    public static void main(String[] args)
    {
        //создаём модель
        Model model = ModelFactory.createDefaultModel();
        //путь к файлу с данными
        model.read( "src/Jena03/resources/example_road/dataset.n3" );

        //создаём резонер. Путь к файлу правил
        Reasoner reasoner = new GenericRuleReasoner( Rule.rulesFromURL( "src/Jena03/resources/example_road/rules.txt" ) );

        //создание модель с правилами
        InfModel infModel = ModelFactory.createInfModel( reasoner, model );
        StmtIterator it = infModel.listStatements();

        //Запись новых данных в файл
        StringBuilder dataSet = new StringBuilder();
        //Обязательно нужен prefix в начале
        dataSet.append("@prefix : <http://tutorialacademy.com/2015/jena#> .\n");

        while ( it.hasNext() )
        {
            Statement stmt = it.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();
            System.out.println( subject.getLocalName() + " "
                    + predicate.getLocalName() + " "
                    + object.asResource().getLocalName() );
            dataSet.append(":").append(subject.getLocalName()).
                    append(" :").append(predicate.getLocalName()).
                    append(" :").append(object.asResource().getLocalName()).append(" .\n");
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
