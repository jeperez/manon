package manon.batch;

import manon.document.user.User;
import manon.document.user.UserSnapshot;
import manon.document.user.UserStats;
import manon.service.batch.JobRunnerService;
import manon.service.user.UserSnapshotService;
import manon.service.user.UserStatsService;
import manon.util.Tools;
import manon.util.basetest.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.batch.core.ExitStatus.COMPLETED;

public class UserSnapshotJobConfigIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private JobRunnerService jobRunnerService;
    @Autowired
    private UserSnapshotService userSnapshotService;
    @Autowired
    private UserStatsService userStatsService;
    
    @Override
    public int getNumberOfUsers() {
        return cfg.getBatchUserSnapshotChunk() * NB_BATCH_CHUNKS_TO_ENSURE_RELIABILITY;
    }
    
    @Test
    public void shouldCompleteMultipleTimes() throws Exception {
        checkComplete(2, 3);
        checkComplete(4, 6);
    }
    
    public void checkComplete(int snapshotsKept, int nbStats) throws Exception {
        int chunk = cfg.getBatchUserSnapshotChunk();
        int maxAge = cfg.getBatchUserSnapshotSnapshotMaxAge();
        assertThat(chunk).isEqualTo(10);
        assertThat(cfg.getBatchUserSnapshotSnapshotMaxAge()).isEqualTo(30);
        User userToSnapshot = userService.readOne(userId(1));
        
        //GIVEN existing old, present, recent and future User snapshots
        List<Integer> delays = Arrays.asList(-1 - maxAge, 0 - maxAge, 1 - maxAge, 0, 1);
        List<UserSnapshot> userSnapshots = new ArrayList<>();
        
        // workaround: can't save custom creationDate at creation, do it at update
        for (int i = 0; i < delays.size(); i++) {
            userSnapshots.add(UserSnapshot.from(userToSnapshot));
        }
        userSnapshotService.save(userSnapshots);
        for (int i = 0; i < delays.size(); i++) {
            userSnapshots.set(i, userSnapshots.get(i).toBuilder().creationDate(Tools.nowPlusDays(delays.get(i))).build());
        }
        userSnapshotService.save(userSnapshots);
        
        long expectedTodayUserSnapshots = userService.count();
        long expectedUserSnapshots = expectedTodayUserSnapshots + snapshotsKept; // keep 1 - maxAge and 0, other are too old, present or future
        for (int i = 0; i < NB_TESTS_TO_ENSURE_BATCH_REPEATABILITY; i++) {
            
            //WHEN run User snapshot job
            ExitStatus run = jobRunnerService.run(UserSnapshotJobConfig.JOB_NAME);
            
            //THEN should keep recent User snapshots only and generate today's ones
            assertThat(run).isEqualTo(COMPLETED);
            assertThat(userSnapshotService.countToday()).isEqualTo(expectedTodayUserSnapshots);
            assertThat(userSnapshotService.count()).isEqualTo(expectedUserSnapshots);
            Stream.of(1, 2).forEach(idx -> assertThat(userSnapshotService.findOne(userSnapshots.get(idx).getId())).isPresent());
            Stream.of(0, 3, 4).forEach(idx -> assertThat(userSnapshotService.findOne(userSnapshots.get(idx).getId())).isNotPresent());
        }
        
        //THEN statistics are written
        List<UserStats> userStats = userStatsService.findAll();
        assertThat(userStats).isNotNull().hasSize(nbStats);
        userStats.forEach(ps -> assertThat(ps.getNbUsers()).isEqualTo(expectedTodayUserSnapshots));
    }
}