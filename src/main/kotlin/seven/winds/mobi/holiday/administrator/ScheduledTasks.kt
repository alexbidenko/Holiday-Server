package seven.winds.mobi.holiday.administrator

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.FileSystemUtils
import seven.winds.mobi.holiday.actions.UpdatedActionRepository
import seven.winds.mobi.holiday.images.FileSystemStorageService
import seven.winds.mobi.holiday.secret.SecretsRepository
import seven.winds.mobi.holiday.tokens.TokensRepository
import java.io.File
import java.nio.file.Paths

@Component
class ScheduledTasks (
        internal val tokensRepository: TokensRepository,
        internal val updatedActionRepository: UpdatedActionRepository,
        internal val secretsRepository: SecretsRepository
) {

    @Scheduled(cron = "0 0 3 * * 6")
    fun clearCashImages() {
        File(Paths.get(FileSystemStorageService.cashLocation).toUri()).listFiles()!!.forEach {
            if(System.currentTimeMillis() -
                    it.name.substringAfterLast("-").substringBeforeLast(".").toLong() > 1000L * 60 * 60 * 24 * 7 &&
                    updatedActionRepository.findOneByImage(it.name) == null) {
                FileSystemUtils.deleteRecursively(it)
            }
        }

        tokensRepository.deleteAllByTimeLessThan(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30)
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    fun clearSecrets() {
        secretsRepository.deleteAllByTimeLessThan(System.currentTimeMillis() - 1000L * 60 * 60 * 24)
    }
}