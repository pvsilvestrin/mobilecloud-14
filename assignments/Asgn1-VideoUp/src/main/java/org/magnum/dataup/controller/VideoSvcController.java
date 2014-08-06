package org.magnum.dataup.controller;

import com.google.common.io.ByteStreams;
import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.VideoSvcApi;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Multipart;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by pvsilvestrin on 04/08/14.
 */
@Controller
public class VideoSvcController  {

    //Video ID Generator
    private static final AtomicLong currentId = new AtomicLong(0L);
    // An in-memory list that the servlet uses to store the
    // videos that are sent to it by clients
    private Map<Long,Video> videos = new HashMap<Long, Video>();

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return videos.values();
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method= RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        v.setId(currentId.incrementAndGet());
        v.setDataUrl(getDataUrl(v.getId()));
        videos.put(v.getId(), v);
        return v;
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method= RequestMethod.POST)
    public @ResponseBody VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
                                                  @RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData) {
        try {
            Video video = videos.get(id);
            if(video == null) throw new ResourceNotFoundException();
            VideoFileManager fileManager = VideoFileManager.get();
            fileManager.saveVideoData(video,videoData.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new VideoStatus(VideoStatus.VideoState.READY);
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method= RequestMethod.GET)
    public void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, HttpServletResponse response) {
        Video video = videos.get(id);
        if(video == null) throw new ResourceNotFoundException();
        try {
            VideoFileManager fileManager = VideoFileManager.get();
            fileManager.copyVideoData(video, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://"+request.getServerName()
                        + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
    }
}
