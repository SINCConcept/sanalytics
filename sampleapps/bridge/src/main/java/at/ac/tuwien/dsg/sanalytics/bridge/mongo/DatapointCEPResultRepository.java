package at.ac.tuwien.dsg.sanalytics.bridge.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import at.ac.tuwien.dsg.sanalytics.events.DatapointCEPResult;

public interface DatapointCEPResultRepository extends MongoRepository<DatapointCEPResult, String> {

}
