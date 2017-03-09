package io.ipoli.android.app.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public abstract class PersistedObject {

    @JsonProperty(value = "_id")
    private String id;
    private String type;
    private Long createdAt;
    private Long updatedAt;
    private List<String> channels;

    public PersistedObject(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void markUpdated() {
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    public List<String> getChannels() {
        return channels != null ? channels : new ArrayList<>();
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    @JsonIgnore
    public void setChannel(String channel) {
        channels = new ArrayList<>();
        channels.add(channel);
    }

    @JsonIgnore
    public void addChannel(String channel) {
        getChannels().add(channel);
    }

    @JsonIgnore
    public void removeChannel(String channel) {
        getChannels().remove(channel);
    }
}
