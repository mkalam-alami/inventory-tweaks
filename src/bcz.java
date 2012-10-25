
/*     */ import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class bcz extends asw
/*     */ {
/*  25 */   private static final Random a = new Random();
/*     */ 
private final static Logger log = Logger.getLogger("InvTweaks");

/*  32 */   private float b = 0.0F;
/*     */   private String c;
/*     */   private arl d;
/*  36 */   private int m = 0;
/*     */   private int n;
/*  39 */   private static final String[] o = { "/title/bg/panorama0.png", "/title/bg/panorama1.png", "/title/bg/panorama2.png", "/title/bg/panorama3.png", "/title/bg/panorama4.png", "/title/bg/panorama5.png" };
/*     */ 
/*     */   @SuppressWarnings({"unchecked", "rawtypes"})
public bcz()
/*     */   {
/*  44 */     this.c = "missingno";
/*     */ 
				// InvTweaks init hook
				try {
				    ModLoader_InvTweaks.init();
				} catch (Exception e) {
				    log.severe("Failed to initialize fake ModLoader by InvTweaks" + e.getMessage());
				}
				
/*  46 */     BufferedReader localBufferedReader = null;
/*     */     try {
/*  48 */       ArrayList localArrayList = new ArrayList();
/*  49 */       localBufferedReader = new BufferedReader(new InputStreamReader(bcz.class.getResourceAsStream("/title/splashes.txt"), Charset.forName("UTF-8")));
/*     */       String str;
/*  51 */       while ((str = localBufferedReader.readLine()) != null) {
/*  52 */         str = str.trim();
/*  53 */         if (str.length() > 0) {
/*  54 */           localArrayList.add(str);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */       do
/*     */       {
/*  62 */         this.c = ((String)localArrayList.get(a.nextInt(localArrayList.size())));
/*  63 */       }while (this.c.hashCode() == 125780783);
/*     */     } catch (IOException localIOException3) {
/*     */     } finally {
/*  66 */       if (localBufferedReader != null)
/*     */         try {
/*  68 */           localBufferedReader.close();
/*     */         }
/*     */         catch (IOException localIOException4)
/*     */         {
/*     */         }
/*     */     }
/*  74 */     this.b = a.nextFloat();
/*     */   }
/*     */ 
/*     */   public void c()
/*     */   {
/*  79 */     this.m += 1;
/*     */   }
/*     */ 
/*     */   public boolean f()
/*     */   {
/*  84 */     return false;
/*     */   }
/*     */ 
/*     */   protected void a(char paramChar, int paramInt)
/*     */   {
/*     */   }
/*     */ 
/*     */   @SuppressWarnings("unchecked")
public void A_()
/*     */   {
/*  93 */     this.n = this.e.o.a(new BufferedImage(256, 256, 2));
/*     */ 
/*  95 */     Calendar localCalendar = Calendar.getInstance();
/*  96 */     localCalendar.setTime(new Date());
/*     */ 
/*  98 */     if ((localCalendar.get(2) + 1 == 11) && (localCalendar.get(5) == 9))
/*  99 */       this.c = "Happy birthday, ez!";
/* 100 */     else if ((localCalendar.get(2) + 1 == 6) && (localCalendar.get(5) == 1))
/* 101 */       this.c = "Happy birthday, Notch!";
/* 102 */     else if ((localCalendar.get(2) + 1 == 12) && (localCalendar.get(5) == 24))
/* 103 */       this.c = "Merry X-mas!";
/* 104 */     else if ((localCalendar.get(2) + 1 == 1) && (localCalendar.get(5) == 1))
/* 105 */       this.c = "Happy new year!";
/* 106 */     else if ((localCalendar.get(2) + 1 == 10) && (localCalendar.get(5) == 31)) {
/* 107 */       this.c = "OOoooOOOoooo! Spooky!";
/*     */     }
/*     */ 
/* 110 */     be localbe = be.a();
/*     */ 
/* 113 */     int i = this.g / 4 + 48;
/*     */ 
/* 115 */     if (this.e.q())
/* 116 */       b(i, 24, localbe);
/*     */     else {
/* 118 */       a(i, 24, localbe);
/*     */     }
/* 120 */     this.h.add(new arl(3, this.f / 2 - 100, i + 48, localbe.b("menu.mods")));
/*     */ 
/* 122 */     if (this.e.m) {
/* 123 */       this.h.add(new arl(0, this.f / 2 - 100, i + 72, localbe.b("menu.options")));
/*     */     } else {
/* 125 */       this.h.add(new arl(0, this.f / 2 - 100, i + 72 + 12, 98, 20, localbe.b("menu.options")));
/* 126 */       this.h.add(new arl(4, this.f / 2 + 2, i + 72 + 12, 98, 20, localbe.b("menu.quit")));
/*     */     }
/* 128 */     this.h.add(new asj(5, this.f / 2 - 124, i + 72 + 12));
/*     */   }
/*     */ 
/*     */   @SuppressWarnings("unchecked")
private void a(int paramInt1, int paramInt2, be parambe)
/*     */   {
/* 133 */     this.h.add(new arl(1, this.f / 2 - 100, paramInt1, parambe.b("menu.singleplayer")));
/* 134 */     this.h.add(new arl(2, this.f / 2 - 100, paramInt1 + paramInt2 * 1, parambe.b("menu.multiplayer")));
/*     */   }
/*     */ 
/*     */   @SuppressWarnings("unchecked")
private void b(int paramInt1, int paramInt2, be parambe) {
/* 138 */     this.h.add(new arl(11, this.f / 2 - 100, paramInt1, parambe.b("menu.playdemo")));
/* 139 */     this.h.add(this.d = new arl(12, this.f / 2 - 100, paramInt1 + paramInt2 * 1, parambe.b("menu.resetdemo")));
/*     */ 
/* 141 */     agy localagy = this.e.d();
/* 142 */     agv localagv = localagy.c("Demo_World");
/* 143 */     if (localagv == null)
/* 144 */       this.d.g = false;
/*     */   }
/*     */ 
/*     */   protected void a(arl paramarl)
/*     */   {
/* 150 */     if (paramarl.f == 0) {
/* 151 */       this.e.a(new aso(this, this.e.y));
/*     */     }
/* 153 */     if (paramarl.f == 5) {
/* 154 */       this.e.a(new ask(this, this.e.y));
/*     */     }
/* 156 */     if (paramarl.f == 1) {
/* 157 */       this.e.a(new asz(this));
/*     */     }
/* 159 */     if (paramarl.f == 2) {
/* 160 */       this.e.a(new asg(this));
/*     */     }
/* 162 */     if (paramarl.f == 3) {
/* 163 */       this.e.a(new bcr(this));
/*     */     }
/* 165 */     if (paramarl.f == 4) {
/* 166 */       this.e.g();
/*     */     }
/* 168 */     if (paramarl.f == 11) {
/* 169 */       this.e.a("Demo_World", "Demo_World", hw.a);
/*     */     }
/* 171 */     if (paramarl.f == 12) {
/* 172 */       agy localagy = this.e.d();
/* 173 */       agv localagv = localagy.c("Demo_World");
/* 174 */       if (localagv != null) {
/* 175 */         arq localarq = asz.a(this, localagv.k(), 12);
/* 176 */         this.e.a(localarq);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(boolean paramBoolean, int paramInt)
/*     */   {
/* 183 */     if ((paramBoolean) && (paramInt == 12)) {
/* 184 */       agy localagy = this.e.d();
/* 185 */       localagy.d();
/* 186 */       localagy.e("Demo_World");
/*     */ 
/* 188 */       this.e.a(this);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void b(int paramInt1, int paramInt2, float paramFloat)
/*     */   {
/* 194 */     azb localazb = azb.a;
/*     */ 
/* 196 */     GL11.glMatrixMode(5889);
/* 197 */     GL11.glPushMatrix();
/* 198 */     GL11.glLoadIdentity();
/* 199 */     GLU.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
/*     */ 
/* 201 */     GL11.glMatrixMode(5888);
/* 202 */     GL11.glPushMatrix();
/* 203 */     GL11.glLoadIdentity();
/* 204 */     GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
/*     */ 
/* 206 */     GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
/* 207 */     GL11.glEnable(3042);
/* 208 */     GL11.glDisable(3008);
/* 209 */     GL11.glDisable(2884);
/* 210 */     GL11.glDepthMask(false);
/* 211 */     GL11.glBlendFunc(770, 771);
/* 212 */     int i = 8;
/*     */ 
/* 214 */     for (int j = 0; j < i * i; j++) {
/* 215 */       GL11.glPushMatrix();
/* 216 */       float f1 = (j % i / i - 0.5F) / 64.0F;
/* 217 */       float f2 = (j / i / i - 0.5F) / 64.0F;
/* 218 */       float f3 = 0.0F;
/* 219 */       GL11.glTranslatef(f1, f2, f3);
/*     */ 
/* 221 */       GL11.glRotatef(jv.a((this.m + paramFloat) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
/* 222 */       GL11.glRotatef(-(this.m + paramFloat) * 0.1F, 0.0F, 1.0F, 0.0F);
/*     */ 
/* 224 */       for (int k = 0; k < 6; k++) {
/* 225 */         GL11.glPushMatrix();
/* 226 */         if (k == 1) GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
/* 227 */         if (k == 2) GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
/* 228 */         if (k == 3) GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
/* 229 */         if (k == 4) GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
/* 230 */         if (k == 5) GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
/* 231 */         GL11.glBindTexture(3553, this.e.o.b(o[k]));
/* 232 */         localazb.b();
/* 233 */         localazb.a(16777215, 255 / (j + 1));
/* 234 */         float f4 = 0.0F;
/* 235 */         localazb.a(-1.0D, -1.0D, 1.0D, 0.0F + f4, 0.0F + f4);
/* 236 */         localazb.a(1.0D, -1.0D, 1.0D, 1.0F - f4, 0.0F + f4);
/* 237 */         localazb.a(1.0D, 1.0D, 1.0D, 1.0F - f4, 1.0F - f4);
/* 238 */         localazb.a(-1.0D, 1.0D, 1.0D, 0.0F + f4, 1.0F - f4);
/* 239 */         localazb.a();
/* 240 */         GL11.glPopMatrix();
/*     */       }
/* 242 */       GL11.glPopMatrix();
/* 243 */       GL11.glColorMask(true, true, true, false);
/*     */     }
/* 245 */     localazb.b(0.0D, 0.0D, 0.0D);
/* 246 */     GL11.glColorMask(true, true, true, true);
/*     */ 
/* 248 */     GL11.glMatrixMode(5889);
/* 249 */     GL11.glPopMatrix();
/* 250 */     GL11.glMatrixMode(5888);
/* 251 */     GL11.glPopMatrix();
/* 252 */     GL11.glDepthMask(true);
/* 253 */     GL11.glEnable(2884);
/*     */ 
/* 255 */     GL11.glEnable(3008);
/* 256 */     GL11.glEnable(2929);
/*     */   }
/*     */ 
/*     */   private void a(float paramFloat) {
/* 260 */     GL11.glBindTexture(3553, this.n);
/* 261 */     GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
/*     */ 
/* 263 */     GL11.glEnable(3042);
/* 264 */     GL11.glBlendFunc(770, 771);
/* 265 */     GL11.glColorMask(true, true, true, false);
/* 266 */     azb localazb = azb.a;
/* 267 */     localazb.b();
/*     */ 
/* 269 */     int i = 3;
/* 270 */     for (int j = 0; j < i; j++) {
/* 271 */       localazb.a(1.0F, 1.0F, 1.0F, 1.0F / (j + 1));
/* 272 */       int k = this.f;
/* 273 */       int i1 = this.g;
/* 274 */       float f = (j - i / 2) / 256.0F;
/* 275 */       localazb.a(k, i1, this.j, 0.0F + f, 0.0D);
/* 276 */       localazb.a(k, 0.0D, this.j, 1.0F + f, 0.0D);
/* 277 */       localazb.a(0.0D, 0.0D, this.j, 1.0F + f, 1.0D);
/* 278 */       localazb.a(0.0D, i1, this.j, 0.0F + f, 1.0D);
/*     */     }
/* 280 */     localazb.a();
/* 281 */     GL11.glColorMask(true, true, true, true);
/*     */   }
/*     */ 
/*     */   private void c(int paramInt1, int paramInt2, float paramFloat) {
/* 285 */     GL11.glViewport(0, 0, 256, 256);
/* 286 */     b(paramInt1, paramInt2, paramFloat);
/* 287 */     GL11.glDisable(3553);
/* 288 */     GL11.glEnable(3553);
/*     */ 
/* 290 */     a(paramFloat);
/* 291 */     a(paramFloat);
/* 292 */     a(paramFloat);
/* 293 */     a(paramFloat);
/* 294 */     a(paramFloat);
/* 295 */     a(paramFloat);
/* 296 */     a(paramFloat);
/* 297 */     a(paramFloat);
/* 298 */     GL11.glViewport(0, 0, this.e.c, this.e.d);
/*     */ 
/* 300 */     azb localazb = azb.a;
/* 301 */     localazb.b();
/*     */ 
/* 303 */     float f1 = this.f > this.g ? 120.0F / this.f : 120.0F / this.g;
/* 304 */     float f2 = this.g * f1 / 256.0F;
/* 305 */     float f3 = this.f * f1 / 256.0F;
/* 306 */     GL11.glTexParameteri(3553, 10241, 9729);
/* 307 */     GL11.glTexParameteri(3553, 10240, 9729);
/* 308 */     localazb.a(1.0F, 1.0F, 1.0F, 1.0F);
/* 309 */     int i = this.f;
/* 310 */     int j = this.g;
/* 311 */     localazb.a(0.0D, j, this.j, 0.5F - f2, 0.5F + f3);
/* 312 */     localazb.a(i, j, this.j, 0.5F - f2, 0.5F - f3);
/* 313 */     localazb.a(i, 0.0D, this.j, 0.5F + f2, 0.5F - f3);
/* 314 */     localazb.a(0.0D, 0.0D, this.j, 0.5F + f2, 0.5F + f3);
/* 315 */     localazb.a();
/*     */   }
/*     */ 
/*     */   public void a(int paramInt1, int paramInt2, float paramFloat)
/*     */   {
/* 320 */     c(paramInt1, paramInt2, paramFloat);
/* 321 */     azb localazb = azb.a;
/*     */ 
/* 323 */     int i = 274;
/* 324 */     int j = this.f / 2 - i / 2;
/* 325 */     int k = 30;
/*     */ 
/* 327 */     a(0, 0, this.f, this.g, -2130706433, 16777215);
/* 328 */     a(0, 0, this.f, this.g, 0, -2147483648);
/*     */ 
/* 330 */     GL11.glBindTexture(3553, this.e.o.b("/title/mclogo.png"));
/* 331 */     GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
/*     */ 
/* 333 */     if (this.b < 0.0001D) {
/* 334 */       b(j + 0, k + 0, 0, 0, 99, 44);
/* 335 */       b(j + 99, k + 0, 129, 0, 27, 44);
/* 336 */       b(j + 99 + 26, k + 0, 126, 0, 3, 44);
/* 337 */       b(j + 99 + 26 + 3, k + 0, 99, 0, 26, 44);
/* 338 */       b(j + 155, k + 0, 0, 45, 155, 44);
/*     */     } else {
/* 340 */       b(j + 0, k + 0, 0, 0, 155, 44);
/* 341 */       b(j + 155, k + 0, 0, 45, 155, 44);
/*     */     }
/*     */ 
/* 344 */     localazb.d(16777215);
/* 345 */     GL11.glPushMatrix();
/* 346 */     GL11.glTranslatef(this.f / 2 + 90, 70.0F, 0.0F);
/*     */ 
/* 348 */     GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
/* 349 */     float f = 1.8F - jv.e(jv.a((float)(Minecraft.F() % 1000L) / 1000.0F * 3.141593F * 2.0F) * 0.1F);
/*     */ 
/* 351 */     f = f * 100.0F / (this.k.a(this.c) + 32);
/* 352 */     GL11.glScalef(f, f, f);
/* 353 */     a(this.k, this.c, 0, -8, 16776960);
/* 354 */     GL11.glPopMatrix();
/*     */ 
/* 356 */     String str1 = "Minecraft 1.4.2";
/* 357 */     if (this.e.q()) {
/* 358 */       str1 = str1 + " Demo";
/*     */     }
/*     */ 
/* 361 */     b(this.k, str1, 2, this.g - 10, 16777215);
/* 362 */     String str2 = "Copyright Mojang AB. Do not distribute!";
/* 363 */     b(this.k, str2, this.f - this.k.a(str2) - 2, this.g - 10, 16777215);
/*     */ 
/* 365 */     super.a(paramInt1, paramInt2, paramFloat);
/*     */   }
/*     */ }

/* Location:           Z:\data\home\mkalam-alami\Perso\mc\src\minecraft142.jar
 * Qualified Name:     bcz
 * JD-Core Version:    0.6.0
 */