// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.util;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;

import hudson.util.ListBoxModel;

/**
 * @author Trek10, Inc.
 */
public class BeanHelper {

    public static ListBoxModel doFillRegionItems() {
        ListBoxModel list = new ListBoxModel();
        for (Region region : RegionUtils.getRegions()) {
            Regions regionData = Regions.fromName(region.getName());
            list.add(regionData.getDescription(), regionData.getName());
        }
        return list;
    }

}
