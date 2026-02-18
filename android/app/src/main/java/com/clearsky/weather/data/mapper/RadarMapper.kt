package com.clearsky.weather.data.mapper

import com.clearsky.weather.data.remote.dto.RadarResponseDto
import com.clearsky.weather.domain.model.RadarData
import com.clearsky.weather.domain.model.RadarFrame

fun RadarResponseDto.toDomain(): RadarData = RadarData(
    generated = generated,
    host = host,
    pastFrames = radar.past.map { frame ->
        RadarFrame(time = frame.time, path = frame.path)
    },
    nowcastFrames = radar.nowcast.map { frame ->
        RadarFrame(time = frame.time, path = frame.path)
    },
    tileUrlTemplate = tileUrlTemplate
)
