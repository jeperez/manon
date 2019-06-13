package manon.api.app;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import manon.service.app.PingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static manon.app.Globals.API.API_SYS;

@Api(description = "Ping service.")
@RestController
@RequestMapping(value = API_SYS)
@RequiredArgsConstructor
public class PingWS {
    
    private final PingService pingService;
    
    @ApiOperation(value = "Check that an URL can be reached.")
    @GetMapping("/ping/{encodedUrl}")
    public void ping(@PathVariable("encodedUrl") String encodedUrl) {
        pingService.ping(encodedUrl);
    }
}
