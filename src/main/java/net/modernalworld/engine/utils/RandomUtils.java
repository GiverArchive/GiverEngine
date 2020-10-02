package net.modernalworld.engine.utils;

import java.util.Random;

public class RandomUtils
{
  private static final Random RANDOM = new Random();
  private static final CharSequence CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  
  @SafeVarargs
  public static <T> T choose(T... ts)
  {
    return ts[nextInt(ts.length)];
  }
  
  public static String generateToken(int length)
  {
    StringBuilder sb = new StringBuilder();
    
    for(int i = 0; i < length; i++)
    {
      sb.append(CHARS.charAt(nextInt(CHARS.length())));
    }
    
    return sb.toString();
  }
  
  public static int nextInt()
  {
    return RANDOM.nextInt();
  }
  
  public static int nextInt(int bound)
  {
    return RANDOM.nextInt(bound);
  }
  
  public static long nextLong()
  {
    return RANDOM.nextLong();
  }
  
  public static boolean nextBoolean()
  {
    return RANDOM.nextBoolean();
  }
  
  public static float nextFloat()
  {
    return RANDOM.nextFloat();
  }
  
  public static double nextDouble()
  {
    return RANDOM.nextDouble();
  }
  
  public static double nextGaussian()
  {
    return RANDOM.nextGaussian();
  }
}
