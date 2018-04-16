package at.ac.tuwien.dsg.sanalytics.meta;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SliceRepository extends PagingAndSortingRepository<Slice, String>{

}
