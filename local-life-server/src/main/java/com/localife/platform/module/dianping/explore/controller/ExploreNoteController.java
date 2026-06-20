package com.localife.platform.module.dianping.explore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.dianping.explore.entity.ExploreNote;
import com.localife.platform.module.dianping.explore.service.ExploreNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-探店笔记", description = "发布/浏览探店笔记")
@RestController
@RequestMapping("/api/customer/explore")
@RequiredArgsConstructor
public class ExploreNoteController {

    private final ExploreNoteService exploreNoteService;

    @Operation(summary = "发布探店笔记")
    @PostMapping
    public Result<Void> publish(@RequestBody ExploreNote note) {
        note.setUserId(UserContext.getUserId());
        exploreNoteService.publish(note);
        return Result.success();
    }

    @Operation(summary = "按店铺查看笔记")
    @GetMapping("/shop/{shopId}")
    public Result<Page<ExploreNote>> byShop(@PathVariable Long shopId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(exploreNoteService.pageByShop(shopId, page, size));
    }

    @Operation(summary = "我的笔记")
    @GetMapping("/my")
    public Result<Page<ExploreNote>> myNotes(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(exploreNoteService.pageByUser(UserContext.getUserId(), page, size));
    }

    @Operation(summary = "笔记详情")
    @GetMapping("/{id}")
    public Result<ExploreNote> detail(@PathVariable Long id) {
        ExploreNote note = exploreNoteService.getById(id);
        return note != null ? Result.success(note) : Result.error("笔记不存在");
    }
}
