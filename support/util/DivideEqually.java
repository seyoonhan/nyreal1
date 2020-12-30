package com.han.startup.support.util;

import com.google.common.base.Preconditions;
import com.ubisoft.hfx.mm.model.PlayerInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("ALL")
public class DivideEqually {

    public static void divide0(int[] arr, int[] set1, int[] set2) {
        Preconditions.checkNotNull(arr);
        Preconditions.checkNotNull(set1);
        Preconditions.checkNotNull(set2);
        int setSize = set1.length;
        Arrays.sort(arr);

        int pos1 = 0;
        int pos2 = 0;

        int i = arr.length - 1;

        int sum1 = 0;
        int sum2 = 0;
        while (pos1 < setSize && pos2 < setSize) {
            if (sum1 < sum2) {
                set1[pos1] = arr[i];
                pos1++;
                sum1 += arr[i];
            } else {
                set2[pos2] = arr[i];
                pos2++;
                sum2 += arr[i];
            }
            i--;
        }

        while (i >= 0) {
            if (pos1 < setSize) {
                set1[pos1++] = arr[i];
            } else {
                set2[pos2++] = arr[i];
            }
            i--;
        }
    }

    public static void divide(List<PlayerInfo> arr, List<PlayerInfo> set1, List<PlayerInfo> set2) {
        Preconditions.checkNotNull(arr);
        Preconditions.checkNotNull(set1);
        Preconditions.checkNotNull(set2);
        Preconditions.checkState(arr.size() > 0);
        Preconditions.checkState(set1.size() == 0);
        Preconditions.checkState(set2.size() == 0);

        int pos1 = 0;
        int pos2 = 0;

        int sum1 = 0;
        int sum2 = 0;

        arr.sort(Comparator.comparing(PlayerInfo::getRankScore));
        if (arr.size() % 2 == 1) {
            double average = arr.stream().mapToInt(o -> o.getRankScore()).average().getAsDouble();
            List<PlayerInfo> belowAverage = arr.stream().filter(o -> o.getRankScore() <= average).collect(Collectors.toList());
            double averageOfPlayersLowerThanAverage = 0;
            if (belowAverage != null || belowAverage.size() > 1) {
                averageOfPlayersLowerThanAverage = belowAverage.stream().mapToInt(o -> o.getRankScore()).average().getAsDouble();
            }

            if (log.isDebugEnabled()) {
                log.debug("average of group:" + average + ", average of players lower than average:" + averageOfPlayersLowerThanAverage);
            }

            sum1 += (int) averageOfPlayersLowerThanAverage;
        }

        int size = (arr.size() + 1) / 2;
        for (int x = 0; x < size; x++) {
            set1.add(null);
            set2.add(null);
        }

        int setSize = set1.size();


        int i = arr.size() - 1;

        while (pos1 < setSize && pos2 < setSize) {
            if (sum1 < sum2) {
                set1.set(pos1, arr.get(i));
                pos1++;
                sum1 += arr.get(i).getRankScore();
            } else {
                set2.set(pos2, arr.get(i));
                pos2++;
                sum2 += arr.get(i).getRankScore();
            }
            i--;
        }

        while (i >= 0) {
            if (pos1 < setSize) {
                set1.set(pos1++, arr.get(i));
            } else {
                set2.set(pos2++, arr.get(i));
            }

            i--;
        }

        set1.remove(null);
        set2.remove(null);
    }


}
