/*     */ /*     */ 
/*     */ public class pg extends lb
/*     */ {
/*     */   public tv a;
/*  23 */   public int b = 0;
/*     */   public int c;
/*  26 */   private int e = 5;
/*     */ 
/*  28 */   public float d = (float)(Math.random() * 3.141592653589793D * 2.0D);
/*     */ 
/*     */   public pg(xe paramxe, double paramDouble1, double paramDouble2, double paramDouble3, tv paramtv) {
/*  31 */     super(paramxe);
/*  32 */     a(0.25F, 0.25F);
/*  33 */     this.M = (this.O / 2.0F);
/*  34 */     b(paramDouble1, paramDouble2, paramDouble3);
/*  35 */     this.a = paramtv;
/*     */ 
/*  37 */     this.z = (float)(Math.random() * 360.0D);
/*     */ 
/*  39 */     this.w = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
/*  40 */     this.x = 0.2000000029802322D;
/*  41 */     this.y = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
/*     */   }
/*     */ 
/*     */   protected boolean f_()
/*     */   {
/*  46 */     return false;
/*     */   }
/*     */ 
/*     */   public pg(xe paramxe) {
/*  50 */     super(paramxe);
/*  51 */     a(0.25F, 0.25F);
/*  52 */     this.M = (this.O / 2.0F);
/*     */   }
/*     */ 
/*     */   protected void a()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void j_()
/*     */   {
/*  61 */     super.j_();
/*  62 */     if (this.c > 0) this.c -= 1;
/*  63 */     this.q = this.t;
/*  64 */     this.r = this.u;
/*  65 */     this.s = this.v;
/*     */ 
/*  67 */     this.x -= 0.03999999910593033D;
/*  68 */     i(this.t, (this.D.b + this.D.e) / 2.0D, this.v);
/*  69 */     d(this.w, this.x, this.y);
/*     */ 
/*  71 */     int i = ((int)this.q != (int)this.t) || ((int)this.r != (int)this.u) || ((int)this.s != (int)this.v) ? 1 : 0;
/*     */ 
/*  73 */     if (i != 0) {
/*  74 */       if (this.p.f(jv.c(this.t), jv.c(this.u), jv.c(this.v)) == afg.i) {
/*  75 */         this.x = 0.2000000029802322D;
/*  76 */         this.w = ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F);
/*  77 */         this.y = ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F);
/*  78 */         this.p.a(this, "random.fizz", 0.4F, 2.0F + this.aa.nextFloat() * 0.4F);
/*     */       }
/*     */ 
/*  81 */       if (!this.p.J) {
/*  82 */         for (Object localpg : this.p.a(pg.class, this.D.b(0.5D, 0.0D, 0.5D))) {
/*  83 */           a((pg) localpg);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  88 */     float f = 0.98F;
/*  89 */     if (this.E) {
/*  90 */       f = 0.5880001F;
/*  91 */       int j = this.p.a(jv.c(this.t), jv.c(this.D.b) - 1, jv.c(this.v));
/*  92 */       if (j > 0) {
/*  93 */         f = alf.p[j].cC * 0.98F;
/*     */       }
/*     */     }
/*     */ 
/*  97 */     this.w *= f;
/*  98 */     this.x *= 0.9800000190734863D;
/*  99 */     this.y *= f;
/*     */ 
/* 101 */     if (this.E) {
/* 102 */       this.x *= -0.5D;
/*     */     }
/*     */ 
/* 105 */     this.b += 1;
/* 106 */     if ((!this.p.J) && (this.b >= 6000))
/* 107 */       x();
/*     */   }
/*     */ 
/*     */   public boolean a(pg parampg)
/*     */   {
/* 112 */     if (parampg == this) return false;
/* 113 */     if ((!parampg.S()) || (!S())) return false;
/* 114 */     if (parampg.a.b() != this.a.b()) return false;
/* 115 */     if ((parampg.a.o()) || (this.a.o())) return false;
/* 116 */     if ((parampg.a.b().l()) && (parampg.a.j() != this.a.j())) return false;
/* 117 */     if (parampg.a.a < this.a.a) return parampg.a(this);
/* 118 */     if (parampg.a.a + this.a.a > parampg.a.d()) return false;
/*     */ 
/* 120 */     parampg.a.a += this.a.a;
/* 121 */     parampg.c = Math.max(parampg.c, this.c);
/* 122 */     parampg.b = Math.min(parampg.b, this.b);
/* 123 */     x();
/*     */ 
/* 125 */     return true;
/*     */   }
/*     */ 
/*     */   public void c()
/*     */   {
/* 130 */     this.b = 4800;
/*     */   }
/*     */ 
/*     */   public boolean I()
/*     */   {
/* 135 */     return this.p.a(this.D, afg.h, this);
/*     */   }
/*     */ 
/*     */   protected void d(int paramInt)
/*     */   {
/* 140 */     a(ks.a, paramInt);
/*     */   }
/*     */ 
/*     */   public boolean a(ks paramks, int paramInt)
/*     */   {
/* 145 */     K();
/* 146 */     this.e -= paramInt;
/* 147 */     if (this.e <= 0) {
/* 148 */       x();
/*     */     }
/* 150 */     return false;
/*     */   }
/*     */ 
/*     */   public void b(bh parambh)
/*     */   {
/* 155 */     parambh.a("Health", (short)(byte)this.e);
/* 156 */     parambh.a("Age", (short)this.b);
/* 157 */     if (this.a != null) parambh.a("Item", this.a.b(new bh()));
/*     */   }
/*     */ 
/*     */   public void a(bh parambh)
/*     */   {
/* 162 */     this.e = (parambh.d("Health") & 0xFF);
/* 163 */     this.b = parambh.d("Age");
/* 164 */     bh localbh = parambh.l("Item");
/* 165 */     this.a = tv.a(localbh);
/* 166 */     if (this.a == null) x();
/*     */   }
/*     */ 
/*     */   public void b_(qg paramqg)
/*     */   {
/* 171 */     if (this.p.J) return;
/*     */ 
/* 173 */     int i = this.a.a;
/* 174 */     if ((this.c == 0) && (paramqg.bK.a(this.a))) {
/* 175 */       if (this.a.c == alf.M.cm) paramqg.a(iy.g);
/* 176 */       if (this.a.c == tt.aF.cf) paramqg.a(iy.t);
/* 177 */       if (this.a.c == tt.n.cf) paramqg.a(iy.w);
/* 178 */       if (this.a.c == tt.bo.cf) paramqg.a(iy.z);

// InvTweaks on pickup hook
ModLoader_InvTweaks.onItemPickup(paramqg, this.a);


/* 179 */       this.p.a(this, "random.pop", 0.2F, ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.7F + 1.0F) * 2.0F);
/* 180 */       paramqg.a(this, i);
/* 181 */       if (this.a.a <= 0) x();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String an()
/*     */   {
/* 187 */     return bd.a("item." + this.a.a());
/*     */   }
/*     */ 
/*     */   public boolean aq()
/*     */   {
/* 192 */     return false;
/*     */   }
/*     */ }