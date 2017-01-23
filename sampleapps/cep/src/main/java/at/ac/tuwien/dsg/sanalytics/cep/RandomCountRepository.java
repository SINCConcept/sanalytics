package at.ac.tuwien.dsg.sanalytics.cep;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RandomCountRepository extends MongoRepository<RandomCount, String> {

}
