package manon.app.trace.repository;

import manon.app.trace.document.AppTrace;
import manon.app.trace.model.AppTraceEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppTraceRepository extends MongoRepository<AppTrace, String> {
    
    long countByAppId(String appId);
    
    void deleteByAppIdAndEvent(String appId, AppTraceEvent event);
    
    long countByAppIdAndEvent(String appId, AppTraceEvent event);
}
