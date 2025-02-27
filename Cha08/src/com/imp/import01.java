package com.imp;//声明当前类所在的包，需要放在最上面。一个类最多一个package

import java.util.Arrays;

public class import01 {
    public static void main(String[] args) {
        int[] arr={0,51,32,17,11,26};
        System.out.println(Arrays.stream(arr).sorted());

        Arrays.sort(arr);
        for (int i=0;i<arr.length;i++) {
        System.out.println(arr[i]+" ");
         }
        for (int j : arr) { System.out.print(j + " "); }
    }
}
