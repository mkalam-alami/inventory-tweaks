/*     */ import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public abstract class atq extends asw
/*     */ {
/*  19 */   protected static bag a = new bag();
/*  20 */   protected int b = 176;
/*  21 */   protected int c = 166;
/*     */   public qy d;
/*     */   protected int m;
/*     */   protected int n;
/*     */   private rz o;
/*     */ 
/*     */   public atq(qy paramqy)
/*     */   {
/*  28 */     this.d = paramqy;
/*     */   }
/*     */ 
/*     */   public void A_()
/*     */   {
/*  33 */     super.A_();
/*  34 */     this.e.g.bM = this.d;
/*     */ 
/*  36 */     this.m = ((this.f - this.b) / 2);
/*  37 */     this.n = ((this.g - this.c) / 2);
/*     */   }
/*     */ 
/*     */   public void a(int paramInt1, int paramInt2, float paramFloat)
/*     */   {
/*  42 */     z_();
/*  43 */     int i = this.m;
/*  44 */     int j = this.n;
/*     */ 
/*  46 */     a(paramFloat, paramInt1, paramInt2);
/*     */ 
/*  48 */     GL11.glDisable(32826);
/*  49 */     aqj.a();
/*  50 */     GL11.glDisable(2896);
/*  51 */     GL11.glDisable(2929);
/*     */ 
/*  53 */     super.a(paramInt1, paramInt2, paramFloat);
/*     */ 
/*  55 */     aqj.c();
/*     */ 
/*  57 */     GL11.glPushMatrix();
/*  58 */     GL11.glTranslatef(i, j, 0.0F);
/*     */ 
/*  60 */     GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
/*  61 */     GL11.glEnable(32826);
/*     */ 
/*  63 */     this.o = null;
/*     */ 
/*  66 */     int k = 240;
/*  67 */     int i1 = 240;
/*  68 */     bdf.a(bdf.b, k / 1.0F, i1 / 1.0F);
/*  69 */     GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
/*     */     Object localObject;
/*  72 */     for (k = 0; k < this.d.b.size(); k++) {
/*  73 */       localObject = (rz)this.d.b.get(k);
/*     */ 
/*  75 */       a((rz)localObject);
/*     */ 
/*  77 */       if (a((rz)localObject, paramInt1, paramInt2)) {
/*  78 */         this.o = ((rz)localObject);
/*     */ 
/*  80 */         GL11.glDisable(2896);
/*  81 */         GL11.glDisable(2929);
/*     */ 
/*  83 */         int i2 = ((rz)localObject).h;
/*  84 */         int i3 = ((rz)localObject).i;
/*  85 */         a(i2, i3, i2 + 16, i3 + 16, -2130706433, -2130706433);
/*  86 */         GL11.glEnable(2896);
/*  87 */         GL11.glEnable(2929);
/*     */       }
/*     */     }
/*     */ 
/*  91 */     b(paramInt1, paramInt2);
/*     */ 
/*  93 */     qf localqf = this.e.g.bK;
/*  94 */     if (localqf.n() != null) {
/*  95 */       GL11.glTranslatef(0.0F, 0.0F, 32.0F);
/*  96 */       this.j = 200.0F;
/*  97 */       a.f = 200.0F;
/*     */ 
/*  99 */       a.b(this.k, this.e.o, localqf.n(), paramInt1 - i - 8, paramInt2 - j - 8);
/* 100 */       a.c(this.k, this.e.o, localqf.n(), paramInt1 - i - 8, paramInt2 - j - 8);
/* 101 */       this.j = 0.0F;
/* 102 */       a.f = 0.0F;
/*     */     }
/*     */ 
/* 105 */     if ((localqf.n() == null) && (this.o != null) && (this.o.d())) {
/* 106 */       localObject = this.o.c();
/* 107 */       a((tv)localObject, paramInt1 - i, paramInt2 - j);
/*     */     }
/*     */ 
/* 110 */     GL11.glPopMatrix();
/*     */ 
/* 112 */     GL11.glEnable(2896);
/* 113 */     GL11.glEnable(2929);
/* 114 */     aqj.b();
/*     */   }
/*     */ 
/*     */   protected void a(tv paramtv, int paramInt1, int paramInt2) {
/* 118 */     GL11.glDisable(32826);
/* 119 */     aqj.a();
/* 120 */     GL11.glDisable(2896);
/* 121 */     GL11.glDisable(2929);
/*     */ 
/* 123 */     List<?> localList = paramtv.a(this.e.g, this.e.y.x);
/* 124 */     if (!localList.isEmpty()) {
/* 125 */       int i = 0;
/* 126 */       for (Object str1 : localList) {
/* 127 */         int i1 = this.k.a((String) str1);
/* 128 */         if (i1 > i) i = i1;
/*     */       }
/*     */ 
/* 131 */       int j = paramInt1 + 12;
/* 132 */       int k = paramInt2 - 12;
/*     */ 
/* 134 */       int i1 = i;
/* 135 */       int i2 = 8;
/* 136 */       if (localList.size() > 1) {
/* 137 */         i2 += 2 + (localList.size() - 1) * 10;
/*     */       }
/*     */ 
/* 140 */       this.j = 300.0F;
/* 141 */       a.f = 300.0F;
/*     */ 
/* 143 */       int i3 = -267386864;
/* 144 */       a(j - 3, k - 4, j + i1 + 3, k - 3, i3, i3);
/* 145 */       a(j - 3, k + i2 + 3, j + i1 + 3, k + i2 + 4, i3, i3);
/*     */ 
/* 147 */       a(j - 3, k - 3, j + i1 + 3, k + i2 + 3, i3, i3);
/* 148 */       a(j - 4, k - 3, j - 3, k + i2 + 3, i3, i3);
/* 149 */       a(j + i1 + 3, k - 3, j + i1 + 4, k + i2 + 3, i3, i3);
/*     */ 
/* 151 */       int i4 = 1347420415;
/* 152 */       int i5 = (i4 & 0xFEFEFE) >> 1 | i4 & 0xFF000000;
/*     */ 
/* 154 */       a(j - 3, k - 3 + 1, j - 3 + 1, k + i2 + 3 - 1, i4, i5);
/* 155 */       a(j + i1 + 2, k - 3 + 1, j + i1 + 3, k + i2 + 3 - 1, i4, i5);
/*     */ 
/* 157 */       a(j - 3, k - 3, j + i1 + 3, k - 3 + 1, i4, i4);
/* 158 */       a(j - 3, k + i2 + 2, j + i1 + 3, k + i2 + 3, i5, i5);
/*     */ 
/* 160 */       for (int i6 = 0; i6 < localList.size(); i6++) {
/* 161 */         String str2 = (String)localList.get(i6);
/*     */ 
/* 163 */         if (i6 == 0)
/* 164 */           str2 = "§" + Integer.toHexString(paramtv.u().e) + str2;
/*     */         else {
/* 166 */           str2 = "§7" + str2;
/*     */         }
/*     */ 
/* 169 */         this.k.a(str2, j, k, -1);
/*     */ 
/* 171 */         if (i6 == 0) k += 2;
/* 172 */         k += 10;
/*     */       }
/*     */ 
/* 175 */       this.j = 0.0F;
/* 176 */       a.f = 0.0F;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void a(String paramString, int paramInt1, int paramInt2) {
/* 181 */     GL11.glDisable(32826);
/* 182 */     aqj.a();
/* 183 */     GL11.glDisable(2896);
/* 184 */     GL11.glDisable(2929);
/*     */ 
/* 186 */     int i = this.k.a(paramString);
/*     */ 
/* 188 */     int j = paramInt1 + 12;
/* 189 */     int k = paramInt2 - 12;
/*     */ 
/* 191 */     int i1 = i;
/* 192 */     int i2 = 8;
/*     */ 
/* 194 */     this.j = 300.0F;
/* 195 */     a.f = 300.0F;
/*     */ 
/* 197 */     int i3 = -267386864;
/* 198 */     a(j - 3, k - 4, j + i1 + 3, k - 3, i3, i3);
/* 199 */     a(j - 3, k + i2 + 3, j + i1 + 3, k + i2 + 4, i3, i3);
/*     */ 
/* 201 */     a(j - 3, k - 3, j + i1 + 3, k + i2 + 3, i3, i3);
/* 202 */     a(j - 4, k - 3, j - 3, k + i2 + 3, i3, i3);
/* 203 */     a(j + i1 + 3, k - 3, j + i1 + 4, k + i2 + 3, i3, i3);
/*     */ 
/* 205 */     int i4 = 1347420415;
/* 206 */     int i5 = (i4 & 0xFEFEFE) >> 1 | i4 & 0xFF000000;
/*     */ 
/* 208 */     a(j - 3, k - 3 + 1, j - 3 + 1, k + i2 + 3 - 1, i4, i5);
/* 209 */     a(j + i1 + 2, k - 3 + 1, j + i1 + 3, k + i2 + 3 - 1, i4, i5);
/*     */ 
/* 211 */     a(j - 3, k - 3, j + i1 + 3, k - 3 + 1, i4, i4);
/* 212 */     a(j - 3, k + i2 + 2, j + i1 + 3, k + i2 + 3, i5, i5);
/*     */ 
/* 214 */     this.k.a(paramString, j, k, -1);
/*     */ 
/* 216 */     this.j = 0.0F;
/* 217 */     a.f = 0.0F;
/*     */   }
/*     */   protected void b(int paramInt1, int paramInt2) {
/*     */   }
/*     */ 
/*     */   protected abstract void a(float paramFloat, int paramInt1, int paramInt2);
/*     */ 
/*     */   private void a(rz paramrz) {
/* 226 */     int i = paramrz.h;
/* 227 */     int j = paramrz.i;
/* 228 */     tv localtv = paramrz.c();
/*     */ 
/* 230 */     int k = 0;
/*     */ 
/* 232 */     this.j = 100.0F;
/* 233 */     a.f = 100.0F;
/* 234 */     if (localtv == null) {
/* 235 */       int i1 = paramrz.b();
/* 236 */       if (i1 >= 0) {
/* 237 */         GL11.glDisable(2896);
/* 238 */         this.e.o.b(this.e.o.b("/gui/items.png"));
/* 239 */         b(i, j, i1 % 16 * 16, i1 / 16 * 16, 16, 16);
/* 240 */         GL11.glEnable(2896);
/* 241 */         k = 1;
/*     */       }
/*     */     }
/*     */ 
/* 245 */     if (k == 0) {
/* 246 */       GL11.glEnable(2929);
/* 247 */       a.b(this.k, this.e.o, localtv, i, j);
/* 248 */       a.c(this.k, this.e.o, localtv, i, j);
/*     */     }
/* 250 */     a.f = 0.0F;
/* 251 */     this.j = 0.0F;
/*     */   }
/*     */ 
/*     */   private rz c(int paramInt1, int paramInt2) {
/* 255 */     for (int i = 0; i < this.d.b.size(); i++) {
/* 256 */       rz localrz = (rz)this.d.b.get(i);
/* 257 */       if (a(localrz, paramInt1, paramInt2)) return localrz;
/*     */     }
/* 259 */     return null;
/*     */   }
/*     */ 
/*     */   protected void a(int paramInt1, int paramInt2, int paramInt3)
/*     */   {
/* 264 */     super.a(paramInt1, paramInt2, paramInt3);
/* 265 */     int i = paramInt3 == this.e.y.M.d + 100 ? 1 : 0;
/*     */ 
/* 267 */     if ((paramInt3 == 0) || (paramInt3 == 1) || (i != 0)) {
/* 268 */       rz localrz = c(paramInt1, paramInt2);
/*     */ 
/* 270 */       int j = this.m;
/* 271 */       int k = this.n;
/* 272 */       int i1 = (paramInt1 < j) || (paramInt2 < k) || (paramInt1 >= j + this.b) || (paramInt2 >= k + this.c) ? 1 : 0;
/*     */ 
/* 274 */       int i2 = -1;
/* 275 */       if (localrz != null) i2 = localrz.g;
/*     */ 
/* 277 */       if (i1 != 0) {
/* 278 */         i2 = -999;
/*     */       }
/*     */ 
/* 281 */       if (i2 != -1)
/* 282 */         if (i != 0) {
/* 283 */           a(localrz, i2, paramInt3, 3);
/*     */         } else {
/* 285 */           int i3 = (i2 != -999) && ((Keyboard.isKeyDown(42)) || (Keyboard.isKeyDown(54))) ? 1 : 0;
/* 286 */           a(localrz, i2, paramInt3, i3 != 0 ? 1 : 0);
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   private boolean a(rz paramrz, int paramInt1, int paramInt2)
/*     */   {
/* 293 */     return c(paramrz.h, paramrz.i, 16, 16, paramInt1, paramInt2);
/*     */   }
/*     */ 
/*     */   protected boolean c(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6) {
/* 297 */     int i = this.m;
/* 298 */     int j = this.n;
/* 299 */     paramInt5 -= i;
/* 300 */     paramInt6 -= j;
/*     */ 
/* 302 */     return (paramInt5 >= paramInt1 - 1) && (paramInt5 < paramInt1 + paramInt3 + 1) && (paramInt6 >= paramInt2 - 1) && (paramInt6 < paramInt2 + paramInt4 + 1);
/*     */   }
/*     */ 
/*     */   protected void a(rz paramrz, int paramInt1, int paramInt2, int paramInt3) {
/* 306 */     if (paramrz != null) paramInt1 = paramrz.g;

			// Inventory Tweaks: filter default action if there's an InvTweaks shortcut to trigger
			if (InvTweaks.getConfigManager().getShortcutsHandler().computeShortcutToTrigger() == null) {
	/* 307 */     this.e.b.a(this.d.c, paramInt1, paramInt2, paramInt3, this.e.g);
				}
/*     */   }
/*     */ 
/*     */   protected void a(char paramChar, int paramInt)
/*     */   {
/* 312 */     if ((paramInt == 1) || (paramInt == this.e.y.F.d)) {
/* 313 */       this.e.g.i();
/*     */     }
/*     */ 
/* 316 */     a(paramInt);
/*     */ 
/* 318 */     if ((paramInt == this.e.y.M.d) && (this.o != null) && (this.o.d()))
/* 319 */       a(this.o, this.o.g, this.c, 3);
/*     */   }
/*     */ 
/*     */   protected boolean a(int paramInt)
/*     */   {
/* 324 */     if ((this.e.g.bK.n() == null) && (this.o != null)) {
/* 325 */       for (int i = 0; i < 9; i++) {
/* 326 */         if (paramInt == 2 + i) {
/* 327 */           a(this.o, this.o.g, i, 2);
/* 328 */           return true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 333 */     return false;
/*     */   }
/*     */ 
/*     */   public void b()
/*     */   {
/* 338 */     if (this.e.g == null) return;
/* 339 */     this.d.a(this.e.g);
/*     */   }
/*     */ 
/*     */   public boolean f()
/*     */   {
/* 344 */     return false;
/*     */   }
/*     */ 
/*     */   public void c()
/*     */   {
/* 349 */     super.c();
/* 350 */     if ((!this.e.g.S()) || (this.e.g.L)) this.e.g.i();
/*     */   }
/*     */ }
