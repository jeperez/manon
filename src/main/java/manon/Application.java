package manon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import manon.app.info.service.InfoService;
import manon.app.stats.service.PerformanceRecorder;
import manon.app.trace.service.AppTraceService;
import manon.user.UserExistsException;
import manon.user.service.UserAdminService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

import static manon.app.config.SpringProfiles.METRICS;
import static manon.app.trace.document.AppTrace.Event.APP_START;
import static manon.app.trace.document.AppTrace.Level.INFO;

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
@EnableCaching
@Slf4j
@RequiredArgsConstructor
public class Application extends SpringBootServletInitializer {
    
    private final UserAdminService userAdminService;
    private final AppTraceService appTraceService;
    private final Environment env;
    private final PerformanceRecorder performanceRecorder;
    private final InfoService infoService;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.web(WebApplicationType.SERVLET).sources(Application.class);
    }
    
    @PostConstruct
    public void initApp() throws UserExistsException {
        infoService.evictCaches();
        String initAppEvent = "Admin username is " + userAdminService.ensureAdmin().getUsername();
        appTraceService.log(INFO, APP_START, initAppEvent);
    }
    
    @PreDestroy
    public void destroy() {
        if (List.of(env.getActiveProfiles()).contains(METRICS) && !performanceRecorder.isEmpty()) {
            log.info(performanceRecorder.showStats());
        }
    }
}
