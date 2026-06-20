package com.localife.platform.module.dianping.explore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.localife.platform.module.dianping.explore.entity.ExploreNote;
import com.localife.platform.module.dianping.explore.mapper.ExploreNoteMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExploreNoteService extends ServiceImpl<ExploreNoteMapper, ExploreNote> {

    public void publish(ExploreNote note) {
        note.setStatus(1);
        note.setCreateTime(LocalDateTime.now());
        save(note);
    }

    public Page<ExploreNote> pageByShop(Long shopId, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<ExploreNote>()
                        .eq(ExploreNote::getShopId, shopId)
                        .eq(ExploreNote::getStatus, 1)
                        .orderByDesc(ExploreNote::getCreateTime));
    }

    public Page<ExploreNote> pageByUser(Long userId, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<ExploreNote>()
                        .eq(ExploreNote::getUserId, userId)
                        .orderByDesc(ExploreNote::getCreateTime));
    }
}
