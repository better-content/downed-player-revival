package com.bettercontent.downedplayerrevival.client;
import java.lang.reflect.Method;
/** Optional presentation-owner bridge; no shared gameplay state or runtime dependency. */
final class SurvivalHudBridge {
 private static Method update,alpha;private static boolean searched;
 private static void find(){if(searched)return;searched=true;try{Class<?> api=Class.forName("com.bettercontent.dynamicsurvivalhud.client.api.DynamicSurvivalHudClientApi");update=api.getMethod("setInjuryHealth",boolean.class,float.class,double.class);alpha=api.getMethod("healthAlpha",float.class);}catch(ReflectiveOperationException ignored){}}
 static void update(boolean door,float health,double probability){find();if(update!=null)try{update.invoke(null,door,health,probability);}catch(ReflectiveOperationException e){throw new IllegalStateException("Injury HUD integration failed",e);}}
 static float alpha(float partial){find();if(alpha!=null)try{return ((Number)alpha.invoke(null,partial)).floatValue();}catch(ReflectiveOperationException e){throw new IllegalStateException("Injury HUD integration failed",e);}return 1;}
}
