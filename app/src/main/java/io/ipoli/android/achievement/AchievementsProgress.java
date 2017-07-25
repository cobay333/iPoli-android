package io.ipoli.android.achievement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class AchievementsProgress extends PersistedObject {

    public static final String TYPE = "achievementProgress";

    private Integer completedQuestCount;
    private ActionCountPerDay completedQuestsInADay;
    private ActionCountPerDay experienceInADay;
    private ActionCountPerDay completedQuestsInARow;
    private ActionCountPerDay completedDailyChallengesInARow;
    private Integer playerLevel;
    private Integer createdChallengeCount;
    private Integer avatarChangedCount;
    private Integer completedDailyChallengeCount;
    private Integer postAddedCount;
    private Integer createdRepeatedQuestCount;
    private Integer rewardUsedCount;

    public AchievementsProgress() {
        super(TYPE);
    }

    public static AchievementsProgress create() {
        AchievementsProgress progress = new AchievementsProgress();
        progress.setCompletedQuestsInADay(new ActionCountPerDay(0, LocalDate.now()));
        progress.setExperienceInADay(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedQuestsInARow(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedDailyChallengesInARow(new ActionCountPerDay(0, LocalDate.now()));
        progress.setCompletedQuestCount(0);
        progress.setAvatarChangedCount(0);
        progress.setCreatedChallengeCount(0);
        progress.setCompletedDailyChallengeCount(0);
        progress.setPostAddedCount(0);
        progress.setCreatedRepeatedQuestCount(0);
        progress.setRewardUsedCount(0);
        progress.setPlayerLevel(Constants.DEFAULT_PLAYER_LEVEL);
        return progress;
    }

    @JsonIgnore
    public void incrementCompletedQuestCount() {
        completedQuestCount++;
    }

    @JsonIgnore
    public void incrementCompletedDailyChallengeCount() {
        completedDailyChallengeCount++;
    }

    @JsonIgnore
    public void incrementChallengeAcceptedCount() {
        createdChallengeCount++;
    }

    @JsonIgnore
    public void incrementAvatarChangedCount() {
        avatarChangedCount++;
    }

    @JsonIgnore
    public void incrementPostAddedCount() {
        postAddedCount++;
    }

    @JsonIgnore
    public void incrementRepeatingQuestAddedCount() {
        createdRepeatedQuestCount++;
    }

    @JsonIgnore
    public void incrementRewardUsedCount() {
        rewardUsedCount++;
    }

    @JsonIgnore
    public void incrementCompletedQuestsInADay() {
        long today = DateUtils.toMillis(LocalDate.now());
        if (completedQuestsInADay.getDate() == today) {
            completedQuestsInADay.increment();
        } else {
            completedQuestsInADay.setDate(today);
            completedQuestsInADay.setCount(1);
        }
    }

    @JsonIgnore
    public void incrementExperienceInADay(int experience) {
        long today = DateUtils.toMillis(LocalDate.now());
        if (experienceInADay.getDate() == today) {
            experienceInADay.increment(experience);
        } else {
            experienceInADay.setDate(today);
            experienceInADay.setCount(experience);
        }
    }

    @JsonIgnore
    public void incrementCompletedQuestsInARow() {
        long today = DateUtils.toMillis(LocalDate.now());
        long yesterday = DateUtils.toMillis(LocalDate.now().minusDays(1));
        if (completedQuestsInARow.getCount() == 0 || completedQuestsInARow.getDate() == yesterday) {
            completedQuestsInARow.increment();
            completedQuestsInARow.setDate(today);
        } else if (completedQuestsInARow.getDate() < yesterday) {
            completedQuestsInARow.setDate(today);
            completedQuestsInARow.setCount(1);
        }
    }

    @JsonIgnore
    public void incrementCompletedDailyChallengesInARow() {
        long today = DateUtils.toMillis(LocalDate.now());
        long yesterday = DateUtils.toMillis(LocalDate.now().minusDays(1));
        if (completedDailyChallengesInARow.getCount() == 0 || completedDailyChallengesInARow.getDate() == yesterday) {
            completedDailyChallengesInARow.increment();
            completedDailyChallengesInARow.setDate(today);
        } else if (completedDailyChallengesInARow.getDate() < yesterday) {
            completedDailyChallengesInARow.setDate(today);
            completedDailyChallengesInARow.setCount(1);
        }
    }

    public Integer getCompletedQuestCount() {
        return completedQuestCount;
    }

    public void setCompletedQuestCount(Integer completedQuestCount) {
        this.completedQuestCount = completedQuestCount;
    }

    public ActionCountPerDay getCompletedQuestsInADay() {
        return completedQuestsInADay;
    }

    public void setCompletedQuestsInADay(ActionCountPerDay completedQuestsInADay) {
        this.completedQuestsInADay = completedQuestsInADay;
    }

    public ActionCountPerDay getExperienceInADay() {
        return experienceInADay;
    }

    public void setExperienceInADay(ActionCountPerDay experienceInADay) {
        this.experienceInADay = experienceInADay;
    }

    public ActionCountPerDay getCompletedQuestsInARow() {
        return completedQuestsInARow;
    }

    public void setCompletedQuestsInARow(ActionCountPerDay completedQuestsInARow) {
        this.completedQuestsInARow = completedQuestsInARow;
    }

    public ActionCountPerDay getCompletedDailyChallengesInARow() {
        return completedDailyChallengesInARow;
    }

    public void setCompletedDailyChallengesInARow(ActionCountPerDay completedDailyChallengesInARow) {
        this.completedDailyChallengesInARow = completedDailyChallengesInARow;
    }

    public Integer getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(Integer playerLevel) {
        this.playerLevel = playerLevel;
    }

    public Integer getCreatedChallengeCount() {
        return createdChallengeCount;
    }

    public void setCreatedChallengeCount(Integer createdChallengeCount) {
        this.createdChallengeCount = createdChallengeCount;
    }

    public Integer getAvatarChangedCount() {
        return avatarChangedCount;
    }

    public void setAvatarChangedCount(Integer avatarChangedCount) {
        this.avatarChangedCount = avatarChangedCount;
    }

    public Integer getCompletedDailyChallengeCount() {
        return completedDailyChallengeCount;
    }

    public void setCompletedDailyChallengeCount(Integer completedDailyChallengeCount) {
        this.completedDailyChallengeCount = completedDailyChallengeCount;
    }

    public Integer getPostAddedCount() {
        return postAddedCount;
    }

    public void setPostAddedCount(Integer postAddedCount) {
        this.postAddedCount = postAddedCount;
    }

    public Integer getCreatedRepeatedQuestCount() {
        return createdRepeatedQuestCount;
    }

    public void setCreatedRepeatedQuestCount(Integer createdRepeatedQuestCount) {
        this.createdRepeatedQuestCount = createdRepeatedQuestCount;
    }

    public Integer getRewardUsedCount() {
        return rewardUsedCount;
    }

    public void setRewardUsedCount(Integer rewardUsedCount) {
        this.rewardUsedCount = rewardUsedCount;
    }
}