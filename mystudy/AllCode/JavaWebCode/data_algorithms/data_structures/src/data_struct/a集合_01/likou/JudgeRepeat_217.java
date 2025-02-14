package data_struct.a集合_01.likou;

import java.util.*;

/**
 * @Classname JudgeRepeat_217
 * @Description TODO
 * @Date 2021/12/26 21:43
 * @Created by zhq
 */
public class JudgeRepeat_217 {
    public static void main(String[] args) {
        int[] nums = {1, 2, 3, 4};
        System.out.println(containsDuplicateThree(nums));
    }

    public static boolean containsDuplicate(int[] nums) {
        if (nums.length == 0) {
            return false;
        }
        Arrays.sort(nums);
        for (int i = 0; i < nums.length - 1; i++) {
            if (nums[i] == nums[i + 1]) {
                return true;
            }
        }
        return false;
    }


    public static boolean containsDuplicateTwo(int[] nums) {
        return Arrays.asList(nums).stream().distinct().count() < nums.length;
    }


    public static boolean containsDuplicateThree(int[] nums) {
        Set set = new HashSet();
        int index = 0;
        while (index < nums.length) {
            if (set.add(nums[index]))
                index++;
           else
                return true;
        }
        return false;
    }
}
