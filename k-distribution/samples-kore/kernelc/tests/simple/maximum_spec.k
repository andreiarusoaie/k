// Copyright (c) 2014-2016 K Team. All Rights Reserved.
module MAXIMUM-SPEC
  imports KERNELC

rule
  <fun>... FUN:Map ...</fun>
  <k>
    maximum(tv(int, X:Int), tv(int, Y:Int))
  =>
    tv(int, maxInt(X, Y))
  ...</k>
  <in> IN:List </in>
  <out> OUT:List </out>

endmodule
