package net.minecraft.server;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

public abstract class Entity {

    private static int entityCount;
    private int id;
    public double j;
    public boolean k;
    public Entity passenger;
    public Entity vehicle;
    public boolean n;
    public World world;
    public double lastX;
    public double lastY;
    public double lastZ;
    public double locX;
    public double locY;
    public double locZ;
    public double motX;
    public double motY;
    public double motZ;
    public float yaw;
    public float pitch;
    public float lastYaw;
    public float lastPitch;
    public final AxisAlignedBB boundingBox;
    public boolean onGround;
    public boolean positionChanged;
    public boolean F;
    public boolean G;
    public boolean velocityChanged;
    protected boolean I;
    public boolean J;
    public boolean dead;
    public float height;
    public float width;
    public float length;
    public float O;
    public float P;
    public float Q;
    public float fallDistance;
    private int d;
    public double S;
    public double T;
    public double U;
    public float V;
    public float W;
    public boolean X;
    public float Y;
    protected Random random;
    public int ticksLived;
    public int maxFireTicks;
    private int fireTicks;
    protected boolean inWater;
    public int noDamageTicks;
    private boolean justCreated;
    protected boolean fireProof;
    protected DataWatcher datawatcher;
    private double g;
    private double h;
    public boolean ag;
    public int ah;
    public int ai;
    public int aj;
    public boolean ak;
    public boolean al;
    public int portalCooldown;
    protected boolean an;
    protected int ao;
    public int dimension;
    protected int aq;
    private boolean invulnerable;
    protected UUID uniqueID;
    public EnumEntitySize as;

    public int getId() {
        return this.id;
    }

    public void d(int i) {
        this.id = i;
    }

    public Entity(World world) {
        this.id = entityCount++;
        this.j = 1.0D;
        this.boundingBox = AxisAlignedBB.a(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        this.J = true;
        this.width = 0.6F;
        this.length = 1.8F;
        this.d = 1;
        this.random = new Random();
        this.maxFireTicks = 1;
        this.justCreated = true;
        this.uniqueID = UUID.randomUUID();
        this.as = EnumEntitySize.SIZE_2;
        this.world = world;
        this.setPosition(0.0D, 0.0D, 0.0D);
        if (world != null) {
            this.dimension = world.worldProvider.dimension;
        }

        this.datawatcher = new DataWatcher(this);
        this.datawatcher.a(0, Byte.valueOf((byte) 0));
        this.datawatcher.a(1, Short.valueOf((short) 300));
        this.c();
    }

    protected abstract void c();

    public DataWatcher getDataWatcher() {
        return this.datawatcher;
    }

    public boolean equals(Object object) {
        return object instanceof Entity ? ((Entity) object).id == this.id : false;
    }

    public int hashCode() {
        return this.id;
    }

    public void die() {
        this.dead = true;
    }

    protected void a(float f, float f1) {
        float f2;

        if (f != this.width || f1 != this.length) {
            f2 = this.width;
            this.width = f;
            this.length = f1;
            this.boundingBox.d = this.boundingBox.a + (double) this.width;
            this.boundingBox.f = this.boundingBox.c + (double) this.width;
            this.boundingBox.e = this.boundingBox.b + (double) this.length;
            if (this.width > f2 && !this.justCreated && !this.world.isStatic) {
                this.move((double) (f2 - this.width), 0.0D, (double) (f2 - this.width));
            }
        }

        f2 = f % 2.0F;
        if ((double) f2 < 0.375D) {
            this.as = EnumEntitySize.SIZE_1;
        } else if ((double) f2 < 0.75D) {
            this.as = EnumEntitySize.SIZE_2;
        } else if ((double) f2 < 1.0D) {
            this.as = EnumEntitySize.SIZE_3;
        } else if ((double) f2 < 1.375D) {
            this.as = EnumEntitySize.SIZE_4;
        } else if ((double) f2 < 1.75D) {
            this.as = EnumEntitySize.SIZE_5;
        } else {
            this.as = EnumEntitySize.SIZE_6;
        }
    }

    protected void b(float f, float f1) {
        this.yaw = f % 360.0F;
        this.pitch = f1 % 360.0F;
    }

    public void setPosition(double d0, double d1, double d2) {
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        float f = this.width / 2.0F;
        float f1 = this.length;

        this.boundingBox.b(d0 - (double) f, d1 - (double) this.height + (double) this.V, d2 - (double) f, d0 + (double) f, d1 - (double) this.height + (double) this.V + (double) f1, d2 + (double) f);
    }

    public void h() {
        this.B();
    }

    public void B() {
        this.world.methodProfiler.a("entityBaseTick");
        if (this.vehicle != null && this.vehicle.dead) {
            this.vehicle = null;
        }

        this.O = this.P;
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.lastPitch = this.pitch;
        this.lastYaw = this.yaw;
        int i;

        if (!this.world.isStatic && this.world instanceof WorldServer) {
            this.world.methodProfiler.a("portal");
            MinecraftServer minecraftserver = ((WorldServer) this.world).getMinecraftServer();

            i = this.C();
            if (this.an) {
                if (minecraftserver.getAllowNether()) {
                    if (this.vehicle == null && this.ao++ >= i) {
                        this.ao = i;
                        this.portalCooldown = this.ah();
                        byte b0;

                        if (this.world.worldProvider.dimension == -1) {
                            b0 = 0;
                        } else {
                            b0 = -1;
                        }

                        this.b(b0);
                    }

                    this.an = false;
                }
            } else {
                if (this.ao > 0) {
                    this.ao -= 4;
                }

                if (this.ao < 0) {
                    this.ao = 0;
                }
            }

            if (this.portalCooldown > 0) {
                --this.portalCooldown;
            }

            this.world.methodProfiler.b();
        }

        if (this.isSprinting() && !this.L()) {
            int j = MathHelper.floor(this.locX);

            i = MathHelper.floor(this.locY - 0.20000000298023224D - (double) this.height);
            int k = MathHelper.floor(this.locZ);
            Block block = this.world.getType(j, i, k);

            if (block.getMaterial() != Material.AIR) {
                this.world.addParticle("blockcrack_" + Block.b(block) + "_" + this.world.getData(j, i, k), this.locX + ((double) this.random.nextFloat() - 0.5D) * (double) this.width, this.boundingBox.b + 0.1D, this.locZ + ((double) this.random.nextFloat() - 0.5D) * (double) this.width, -this.motX * 4.0D, 1.5D, -this.motZ * 4.0D);
            }
        }

        this.M();
        if (this.world.isStatic) {
            this.fireTicks = 0;
        } else if (this.fireTicks > 0) {
            if (this.fireProof) {
                this.fireTicks -= 4;
                if (this.fireTicks < 0) {
                    this.fireTicks = 0;
                }
            } else {
                if (this.fireTicks % 20 == 0) {
                    this.damageEntity(DamageSource.BURN, 1.0F);
                }

                --this.fireTicks;
            }
        }

        if (this.O()) {
            this.D();
            this.fallDistance *= 0.5F;
        }

        if (this.locY < -64.0D) {
            this.F();
        }

        if (!this.world.isStatic) {
            this.a(0, this.fireTicks > 0);
        }

        this.justCreated = false;
        this.world.methodProfiler.b();
    }

    public int C() {
        return 0;
    }

    protected void D() {
        if (!this.fireProof) {
            this.damageEntity(DamageSource.LAVA, 4.0F);
            this.setOnFire(15);
        }
    }

    public void setOnFire(int i) {
        int j = i * 20;

        j = EnchantmentProtection.a(this, j);
        if (this.fireTicks < j) {
            this.fireTicks = j;
        }
    }

    public void extinguish() {
        this.fireTicks = 0;
    }

    protected void F() {
        this.die();
    }

    public boolean c(double d0, double d1, double d2) {
        AxisAlignedBB axisalignedbb = this.boundingBox.c(d0, d1, d2);
        List list = this.world.getCubes(this, axisalignedbb);

        return !list.isEmpty() ? false : !this.world.containsLiquid(axisalignedbb);
    }

    public void move(double d0, double d1, double d2) {
        if (this.X) {
            this.boundingBox.d(d0, d1, d2);
            this.locX = (this.boundingBox.a + this.boundingBox.d) / 2.0D;
            this.locY = this.boundingBox.b + (double) this.height - (double) this.V;
            this.locZ = (this.boundingBox.c + this.boundingBox.f) / 2.0D;
        } else {
            this.world.methodProfiler.a("move");
            this.V *= 0.4F;
            double d3 = this.locX;
            double d4 = this.locY;
            double d5 = this.locZ;

            if (this.I) {
                this.I = false;
                d0 *= 0.25D;
                d1 *= 0.05000000074505806D;
                d2 *= 0.25D;
                this.motX = 0.0D;
                this.motY = 0.0D;
                this.motZ = 0.0D;
            }

            double d6 = d0;
            double d7 = d1;
            double d8 = d2;
            AxisAlignedBB axisalignedbb = this.boundingBox.clone();
            boolean flag = this.onGround && this.isSneaking() && this instanceof EntityHuman;

            if (flag) {
                double d9;

                for (d9 = 0.05D; d0 != 0.0D && this.world.getCubes(this, this.boundingBox.c(d0, -1.0D, 0.0D)).isEmpty(); d6 = d0) {
                    if (d0 < d9 && d0 >= -d9) {
                        d0 = 0.0D;
                    } else if (d0 > 0.0D) {
                        d0 -= d9;
                    } else {
                        d0 += d9;
                    }
                }

                for (; d2 != 0.0D && this.world.getCubes(this, this.boundingBox.c(0.0D, -1.0D, d2)).isEmpty(); d8 = d2) {
                    if (d2 < d9 && d2 >= -d9) {
                        d2 = 0.0D;
                    } else if (d2 > 0.0D) {
                        d2 -= d9;
                    } else {
                        d2 += d9;
                    }
                }

                while (d0 != 0.0D && d2 != 0.0D && this.world.getCubes(this, this.boundingBox.c(d0, -1.0D, d2)).isEmpty()) {
                    if (d0 < d9 && d0 >= -d9) {
                        d0 = 0.0D;
                    } else if (d0 > 0.0D) {
                        d0 -= d9;
                    } else {
                        d0 += d9;
                    }

                    if (d2 < d9 && d2 >= -d9) {
                        d2 = 0.0D;
                    } else if (d2 > 0.0D) {
                        d2 -= d9;
                    } else {
                        d2 += d9;
                    }

                    d6 = d0;
                    d8 = d2;
                }
            }

            List list = this.world.getCubes(this, this.boundingBox.a(d0, d1, d2));

            for (int i = 0; i < list.size(); ++i) {
                d1 = ((AxisAlignedBB) list.get(i)).b(this.boundingBox, d1);
            }

            this.boundingBox.d(0.0D, d1, 0.0D);
            if (!this.J && d7 != d1) {
                d2 = 0.0D;
                d1 = 0.0D;
                d0 = 0.0D;
            }

            boolean flag1 = this.onGround || d7 != d1 && d7 < 0.0D;

            int j;

            for (j = 0; j < list.size(); ++j) {
                d0 = ((AxisAlignedBB) list.get(j)).a(this.boundingBox, d0);
            }

            this.boundingBox.d(d0, 0.0D, 0.0D);
            if (!this.J && d6 != d0) {
                d2 = 0.0D;
                d1 = 0.0D;
                d0 = 0.0D;
            }

            for (j = 0; j < list.size(); ++j) {
                d2 = ((AxisAlignedBB) list.get(j)).c(this.boundingBox, d2);
            }

            this.boundingBox.d(0.0D, 0.0D, d2);
            if (!this.J && d8 != d2) {
                d2 = 0.0D;
                d1 = 0.0D;
                d0 = 0.0D;
            }

            double d10;
            double d11;
            double d12;
            int k;

            if (this.W > 0.0F && flag1 && (flag || this.V < 0.05F) && (d6 != d0 || d8 != d2)) {
                d10 = d0;
                d11 = d1;
                d12 = d2;
                d0 = d6;
                d1 = (double) this.W;
                d2 = d8;
                AxisAlignedBB axisalignedbb1 = this.boundingBox.clone();

                this.boundingBox.d(axisalignedbb);
                list = this.world.getCubes(this, this.boundingBox.a(d6, d1, d8));

                for (k = 0; k < list.size(); ++k) {
                    d1 = ((AxisAlignedBB) list.get(k)).b(this.boundingBox, d1);
                }

                this.boundingBox.d(0.0D, d1, 0.0D);
                if (!this.J && d7 != d1) {
                    d2 = 0.0D;
                    d1 = 0.0D;
                    d0 = 0.0D;
                }

                for (k = 0; k < list.size(); ++k) {
                    d0 = ((AxisAlignedBB) list.get(k)).a(this.boundingBox, d0);
                }

                this.boundingBox.d(d0, 0.0D, 0.0D);
                if (!this.J && d6 != d0) {
                    d2 = 0.0D;
                    d1 = 0.0D;
                    d0 = 0.0D;
                }

                for (k = 0; k < list.size(); ++k) {
                    d2 = ((AxisAlignedBB) list.get(k)).c(this.boundingBox, d2);
                }

                this.boundingBox.d(0.0D, 0.0D, d2);
                if (!this.J && d8 != d2) {
                    d2 = 0.0D;
                    d1 = 0.0D;
                    d0 = 0.0D;
                }

                if (!this.J && d7 != d1) {
                    d2 = 0.0D;
                    d1 = 0.0D;
                    d0 = 0.0D;
                } else {
                    d1 = (double) (-this.W);

                    for (k = 0; k < list.size(); ++k) {
                        d1 = ((AxisAlignedBB) list.get(k)).b(this.boundingBox, d1);
                    }

                    this.boundingBox.d(0.0D, d1, 0.0D);
                }

                if (d10 * d10 + d12 * d12 >= d0 * d0 + d2 * d2) {
                    d0 = d10;
                    d1 = d11;
                    d2 = d12;
                    this.boundingBox.d(axisalignedbb1);
                }
            }

            this.world.methodProfiler.b();
            this.world.methodProfiler.a("rest");
            this.locX = (this.boundingBox.a + this.boundingBox.d) / 2.0D;
            this.locY = this.boundingBox.b + (double) this.height - (double) this.V;
            this.locZ = (this.boundingBox.c + this.boundingBox.f) / 2.0D;
            this.positionChanged = d6 != d0 || d8 != d2;
            this.F = d7 != d1;
            this.onGround = d7 != d1 && d7 < 0.0D;
            this.G = this.positionChanged || this.F;
            this.a(d1, this.onGround);
            if (d6 != d0) {
                this.motX = 0.0D;
            }

            if (d7 != d1) {
                this.motY = 0.0D;
            }

            if (d8 != d2) {
                this.motZ = 0.0D;
            }

            d10 = this.locX - d3;
            d11 = this.locY - d4;
            d12 = this.locZ - d5;
            if (this.g_() && !flag && this.vehicle == null) {
                int l = MathHelper.floor(this.locX);

                k = MathHelper.floor(this.locY - 0.20000000298023224D - (double) this.height);
                int i1 = MathHelper.floor(this.locZ);
                Block block = this.world.getType(l, k, i1);
                int j1 = this.world.getType(l, k - 1, i1).b();

                if (j1 == 11 || j1 == 32 || j1 == 21) {
                    block = this.world.getType(l, k - 1, i1);
                }

                if (block != Blocks.LADDER) {
                    d11 = 0.0D;
                }

                this.P = (float) ((double) this.P + (double) MathHelper.sqrt(d10 * d10 + d12 * d12) * 0.6D);
                this.Q = (float) ((double) this.Q + (double) MathHelper.sqrt(d10 * d10 + d11 * d11 + d12 * d12) * 0.6D);
                if (this.Q > (float) this.d && block.getMaterial() != Material.AIR) {
                    this.d = (int) this.Q + 1;
                    if (this.L()) {
                        float f = MathHelper.sqrt(this.motX * this.motX * 0.20000000298023224D + this.motY * this.motY + this.motZ * this.motZ * 0.20000000298023224D) * 0.35F;

                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        this.makeSound(this.G(), f, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                    }

                    this.a(l, k, i1, block);
                    block.b(this.world, l, k, i1, this);
                }
            }

            try {
                this.H();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Checking entity block collision");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being checked for collision");

                this.a(crashreportsystemdetails);
                throw new ReportedException(crashreport);
            }

            boolean flag2 = this.K();

            if (this.world.e(this.boundingBox.shrink(0.001D, 0.001D, 0.001D))) {
                this.burn(1);
                if (!flag2) {
                    ++this.fireTicks;
                    if (this.fireTicks == 0) {
                        this.setOnFire(8);
                    }
                }
            } else if (this.fireTicks <= 0) {
                this.fireTicks = -this.maxFireTicks;
            }

            if (flag2 && this.fireTicks > 0) {
                this.makeSound("random.fizz", 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                this.fireTicks = -this.maxFireTicks;
            }

            this.world.methodProfiler.b();
        }
    }

    protected String G() {
        return "game.neutral.swim";
    }

    protected void H() {
        int i = MathHelper.floor(this.boundingBox.a + 0.001D);
        int j = MathHelper.floor(this.boundingBox.b + 0.001D);
        int k = MathHelper.floor(this.boundingBox.c + 0.001D);
        int l = MathHelper.floor(this.boundingBox.d - 0.001D);
        int i1 = MathHelper.floor(this.boundingBox.e - 0.001D);
        int j1 = MathHelper.floor(this.boundingBox.f - 0.001D);

        if (this.world.b(i, j, k, l, i1, j1)) {
            for (int k1 = i; k1 <= l; ++k1) {
                for (int l1 = j; l1 <= i1; ++l1) {
                    for (int i2 = k; i2 <= j1; ++i2) {
                        Block block = this.world.getType(k1, l1, i2);

                        try {
                            block.a(this.world, k1, l1, i2, this);
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.a(throwable, "Colliding entity with block");
                            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being collided with");

                            CrashReportSystemDetails.a(crashreportsystemdetails, k1, l1, i2, block, this.world.getData(k1, l1, i2));
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }
    }

    protected void a(int i, int j, int k, Block block) {
        StepSound stepsound = block.stepSound;

        if (this.world.getType(i, j + 1, k) == Blocks.SNOW) {
            stepsound = Blocks.SNOW.stepSound;
            this.makeSound(stepsound.getStepSound(), stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
        } else if (!block.getMaterial().isLiquid()) {
            this.makeSound(stepsound.getStepSound(), stepsound.getVolume1() * 0.15F, stepsound.getVolume2());
        }
    }

    public void makeSound(String s, float f, float f1) {
        this.world.makeSound(this, s, f, f1);
    }

    protected boolean g_() {
        return true;
    }

    protected void a(double d0, boolean flag) {
        if (flag) {
            if (this.fallDistance > 0.0F) {
                this.b(this.fallDistance);
                this.fallDistance = 0.0F;
            }
        } else if (d0 < 0.0D) {
            this.fallDistance = (float) ((double) this.fallDistance - d0);
        }
    }

    public AxisAlignedBB I() {
        return null;
    }

    protected void burn(int i) {
        if (!this.fireProof) {
            this.damageEntity(DamageSource.FIRE, (float) i);
        }
    }

    public final boolean isFireproof() {
        return this.fireProof;
    }

    protected void b(float f) {
        if (this.passenger != null) {
            this.passenger.b(f);
        }
    }

    public boolean K() {
        return this.inWater || this.world.isRainingAt(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ)) || this.world.isRainingAt(MathHelper.floor(this.locX), MathHelper.floor(this.locY + (double) this.length), MathHelper.floor(this.locZ));
    }

    public boolean L() {
        return this.inWater;
    }

    public boolean M() {
        if (this.world.a(this.boundingBox.grow(0.0D, -0.4000000059604645D, 0.0D).shrink(0.001D, 0.001D, 0.001D), Material.WATER, this)) {
            if (!this.inWater && !this.justCreated) {
                float f = MathHelper.sqrt(this.motX * this.motX * 0.20000000298023224D + this.motY * this.motY + this.motZ * this.motZ * 0.20000000298023224D) * 0.2F;

                if (f > 1.0F) {
                    f = 1.0F;
                }

                this.makeSound(this.N(), f, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                float f1 = (float) MathHelper.floor(this.boundingBox.b);

                int i;
                float f2;
                float f3;

                for (i = 0; (float) i < 1.0F + this.width * 20.0F; ++i) {
                    f2 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
                    f3 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
                    this.world.addParticle("bubble", this.locX + (double) f2, (double) (f1 + 1.0F), this.locZ + (double) f3, this.motX, this.motY - (double) (this.random.nextFloat() * 0.2F), this.motZ);
                }

                for (i = 0; (float) i < 1.0F + this.width * 20.0F; ++i) {
                    f2 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
                    f3 = (this.random.nextFloat() * 2.0F - 1.0F) * this.width;
                    this.world.addParticle("splash", this.locX + (double) f2, (double) (f1 + 1.0F), this.locZ + (double) f3, this.motX, this.motY, this.motZ);
                }
            }

            this.fallDistance = 0.0F;
            this.inWater = true;
            this.fireTicks = 0;
        } else {
            this.inWater = false;
        }

        return this.inWater;
    }

    protected String N() {
        return "game.neutral.swim.splash";
    }

    public boolean a(Material material) {
        double d0 = this.locY + (double) this.getHeadHeight();
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.d((float) MathHelper.floor(d0));
        int k = MathHelper.floor(this.locZ);
        Block block = this.world.getType(i, j, k);

        if (block.getMaterial() == material) {
            float f = BlockFluids.b(this.world.getData(i, j, k)) - 0.11111111F;
            float f1 = (float) (j + 1) - f;

            return d0 < (double) f1;
        } else {
            return false;
        }
    }

    public float getHeadHeight() {
        return 0.0F;
    }

    public boolean O() {
        return this.world.a(this.boundingBox.grow(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.LAVA);
    }

    public void a(float f, float f1, float f2) {
        float f3 = f * f + f1 * f1;

        if (f3 >= 1.0E-4F) {
            f3 = MathHelper.c(f3);
            if (f3 < 1.0F) {
                f3 = 1.0F;
            }

            f3 = f2 / f3;
            f *= f3;
            f1 *= f3;
            float f4 = MathHelper.sin(this.yaw * 3.1415927F / 180.0F);
            float f5 = MathHelper.cos(this.yaw * 3.1415927F / 180.0F);

            this.motX += (double) (f * f5 - f1 * f4);
            this.motZ += (double) (f1 * f5 + f * f4);
        }
    }

    public float d(float f) {
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.locZ);

        if (this.world.isLoaded(i, 0, j)) {
            double d0 = (this.boundingBox.e - this.boundingBox.b) * 0.66D;
            int k = MathHelper.floor(this.locY - (double) this.height + d0);

            return this.world.n(i, k, j);
        } else {
            return 0.0F;
        }
    }

    public void spawnIn(World world) {
        this.world = world;
    }

    public void setLocation(double d0, double d1, double d2, float f, float f1) {
        this.lastX = this.locX = d0;
        this.lastY = this.locY = d1;
        this.lastZ = this.locZ = d2;
        this.lastYaw = this.yaw = f;
        this.lastPitch = this.pitch = f1;
        this.V = 0.0F;
        double d3 = (double) (this.lastYaw - f);

        if (d3 < -180.0D) {
            this.lastYaw += 360.0F;
        }

        if (d3 >= 180.0D) {
            this.lastYaw -= 360.0F;
        }

        this.setPosition(this.locX, this.locY, this.locZ);
        this.b(f, f1);
    }

    public void setPositionRotation(double d0, double d1, double d2, float f, float f1) {
        this.S = this.lastX = this.locX = d0;
        this.T = this.lastY = this.locY = d1 + (double) this.height;
        this.U = this.lastZ = this.locZ = d2;
        this.yaw = f;
        this.pitch = f1;
        this.setPosition(this.locX, this.locY, this.locZ);
    }

    public float e(Entity entity) {
        float f = (float) (this.locX - entity.locX);
        float f1 = (float) (this.locY - entity.locY);
        float f2 = (float) (this.locZ - entity.locZ);

        return MathHelper.c(f * f + f1 * f1 + f2 * f2);
    }

    public double e(double d0, double d1, double d2) {
        double d3 = this.locX - d0;
        double d4 = this.locY - d1;
        double d5 = this.locZ - d2;

        return d3 * d3 + d4 * d4 + d5 * d5;
    }

    public double f(double d0, double d1, double d2) {
        double d3 = this.locX - d0;
        double d4 = this.locY - d1;
        double d5 = this.locZ - d2;

        return (double) MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
    }

    public double f(Entity entity) {
        double d0 = this.locX - entity.locX;
        double d1 = this.locY - entity.locY;
        double d2 = this.locZ - entity.locZ;

        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public void b_(EntityHuman entityhuman) {}

    public void collide(Entity entity) {
        if (entity.passenger != this && entity.vehicle != this) {
            double d0 = entity.locX - this.locX;
            double d1 = entity.locZ - this.locZ;
            double d2 = MathHelper.a(d0, d1);

            if (d2 >= 0.009999999776482582D) {
                d2 = (double) MathHelper.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;

                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;
                d0 *= (double) (1.0F - this.Y);
                d1 *= (double) (1.0F - this.Y);
                this.g(-d0, 0.0D, -d1);
                entity.g(d0, 0.0D, d1);
            }
        }
    }

    public void g(double d0, double d1, double d2) {
        this.motX += d0;
        this.motY += d1;
        this.motZ += d2;
        this.al = true;
    }

    protected void P() {
        this.velocityChanged = true;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable()) {
            return false;
        } else {
            this.P();
            return false;
        }
    }

    public boolean Q() {
        return false;
    }

    public boolean R() {
        return false;
    }

    public void b(Entity entity, int i) {}

    public boolean c(NBTTagCompound nbttagcompound) {
        String s = this.V();

        if (!this.dead && s != null) {
            nbttagcompound.setString("id", s);
            this.e(nbttagcompound);
            return true;
        } else {
            return false;
        }
    }

    public boolean d(NBTTagCompound nbttagcompound) {
        String s = this.V();

        if (!this.dead && s != null && this.passenger == null) {
            nbttagcompound.setString("id", s);
            this.e(nbttagcompound);
            return true;
        } else {
            return false;
        }
    }

    public void e(NBTTagCompound nbttagcompound) {
        try {
            nbttagcompound.set("Pos", this.a(new double[] { this.locX, this.locY + (double) this.V, this.locZ}));
            nbttagcompound.set("Motion", this.a(new double[] { this.motX, this.motY, this.motZ}));
            nbttagcompound.set("Rotation", this.a(new float[] { this.yaw, this.pitch}));
            nbttagcompound.setFloat("FallDistance", this.fallDistance);
            nbttagcompound.setShort("Fire", (short) this.fireTicks);
            nbttagcompound.setShort("Air", (short) this.getAirTicks());
            nbttagcompound.setBoolean("OnGround", this.onGround);
            nbttagcompound.setInt("Dimension", this.dimension);
            nbttagcompound.setBoolean("Invulnerable", this.invulnerable);
            nbttagcompound.setInt("PortalCooldown", this.portalCooldown);
            nbttagcompound.setLong("UUIDMost", this.getUniqueID().getMostSignificantBits());
            nbttagcompound.setLong("UUIDLeast", this.getUniqueID().getLeastSignificantBits());
            this.b(nbttagcompound);
            if (this.vehicle != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                if (this.vehicle.c(nbttagcompound1)) {
                    nbttagcompound.set("Riding", nbttagcompound1);
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Saving entity NBT");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being saved");

            this.a(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    public void f(NBTTagCompound nbttagcompound) {
        try {
            NBTTagList nbttaglist = nbttagcompound.getList("Pos", 6);
            NBTTagList nbttaglist1 = nbttagcompound.getList("Motion", 6);
            NBTTagList nbttaglist2 = nbttagcompound.getList("Rotation", 5);

            this.motX = nbttaglist1.d(0);
            this.motY = nbttaglist1.d(1);
            this.motZ = nbttaglist1.d(2);
            if (Math.abs(this.motX) > 10.0D) {
                this.motX = 0.0D;
            }

            if (Math.abs(this.motY) > 10.0D) {
                this.motY = 0.0D;
            }

            if (Math.abs(this.motZ) > 10.0D) {
                this.motZ = 0.0D;
            }

            this.lastX = this.S = this.locX = nbttaglist.d(0);
            this.lastY = this.T = this.locY = nbttaglist.d(1);
            this.lastZ = this.U = this.locZ = nbttaglist.d(2);
            this.lastYaw = this.yaw = nbttaglist2.e(0);
            this.lastPitch = this.pitch = nbttaglist2.e(1);
            this.fallDistance = nbttagcompound.getFloat("FallDistance");
            this.fireTicks = nbttagcompound.getShort("Fire");
            this.setAirTicks(nbttagcompound.getShort("Air"));
            this.onGround = nbttagcompound.getBoolean("OnGround");
            this.dimension = nbttagcompound.getInt("Dimension");
            this.invulnerable = nbttagcompound.getBoolean("Invulnerable");
            this.portalCooldown = nbttagcompound.getInt("PortalCooldown");
            if (nbttagcompound.hasKeyOfType("UUIDMost", 4) && nbttagcompound.hasKeyOfType("UUIDLeast", 4)) {
                this.uniqueID = new UUID(nbttagcompound.getLong("UUIDMost"), nbttagcompound.getLong("UUIDLeast"));
            }

            this.setPosition(this.locX, this.locY, this.locZ);
            this.b(this.yaw, this.pitch);
            this.a(nbttagcompound);
            if (this.U()) {
                this.setPosition(this.locX, this.locY, this.locZ);
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Loading entity NBT");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being loaded");

            this.a(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    protected boolean U() {
        return true;
    }

    protected final String V() {
        return EntityTypes.b(this);
    }

    protected abstract void a(NBTTagCompound nbttagcompound);

    protected abstract void b(NBTTagCompound nbttagcompound);

    public void W() {}

    protected NBTTagList a(double... adouble) {
        NBTTagList nbttaglist = new NBTTagList();
        double[] adouble1 = adouble;
        int i = adouble.length;

        for (int j = 0; j < i; ++j) {
            double d0 = adouble1[j];

            nbttaglist.add(new NBTTagDouble(d0));
        }

        return nbttaglist;
    }

    protected NBTTagList a(float... afloat) {
        NBTTagList nbttaglist = new NBTTagList();
        float[] afloat1 = afloat;
        int i = afloat.length;

        for (int j = 0; j < i; ++j) {
            float f = afloat1[j];

            nbttaglist.add(new NBTTagFloat(f));
        }

        return nbttaglist;
    }

    public EntityItem a(Item item, int i) {
        return this.a(item, i, 0.0F);
    }

    public EntityItem a(Item item, int i, float f) {
        return this.a(new ItemStack(item, i, 0), f);
    }

    public EntityItem a(ItemStack itemstack, float f) {
        if (itemstack.count != 0 && itemstack.getItem() != null) {
            EntityItem entityitem = new EntityItem(this.world, this.locX, this.locY + (double) f, this.locZ, itemstack);

            entityitem.pickupDelay = 10;
            this.world.addEntity(entityitem);
            return entityitem;
        } else {
            return null;
        }
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public boolean inBlock() {
        for (int i = 0; i < 8; ++i) {
            float f = ((float) ((i >> 0) % 2) - 0.5F) * this.width * 0.8F;
            float f1 = ((float) ((i >> 1) % 2) - 0.5F) * 0.1F;
            float f2 = ((float) ((i >> 2) % 2) - 0.5F) * this.width * 0.8F;
            int j = MathHelper.floor(this.locX + (double) f);
            int k = MathHelper.floor(this.locY + (double) this.getHeadHeight() + (double) f1);
            int l = MathHelper.floor(this.locZ + (double) f2);

            if (this.world.getType(j, k, l).r()) {
                return true;
            }
        }

        return false;
    }

    public boolean c(EntityHuman entityhuman) {
        return false;
    }

    public AxisAlignedBB h(Entity entity) {
        return null;
    }

    public void aa() {
        if (this.vehicle.dead) {
            this.vehicle = null;
        } else {
            this.motX = 0.0D;
            this.motY = 0.0D;
            this.motZ = 0.0D;
            this.h();
            if (this.vehicle != null) {
                this.vehicle.ab();
                this.h += (double) (this.vehicle.yaw - this.vehicle.lastYaw);

                for (this.g += (double) (this.vehicle.pitch - this.vehicle.lastPitch); this.h >= 180.0D; this.h -= 360.0D) {
                    ;
                }

                while (this.h < -180.0D) {
                    this.h += 360.0D;
                }

                while (this.g >= 180.0D) {
                    this.g -= 360.0D;
                }

                while (this.g < -180.0D) {
                    this.g += 360.0D;
                }

                double d0 = this.h * 0.5D;
                double d1 = this.g * 0.5D;
                float f = 10.0F;

                if (d0 > (double) f) {
                    d0 = (double) f;
                }

                if (d0 < (double) (-f)) {
                    d0 = (double) (-f);
                }

                if (d1 > (double) f) {
                    d1 = (double) f;
                }

                if (d1 < (double) (-f)) {
                    d1 = (double) (-f);
                }

                this.h -= d0;
                this.g -= d1;
            }
        }
    }

    public void ab() {
        if (this.passenger != null) {
            this.passenger.setPosition(this.locX, this.locY + this.ad() + this.passenger.ac(), this.locZ);
        }
    }

    public double ac() {
        return (double) this.height;
    }

    public double ad() {
        return (double) this.length * 0.75D;
    }

    public void mount(Entity entity) {
        this.g = 0.0D;
        this.h = 0.0D;
        if (entity == null) {
            if (this.vehicle != null) {
                this.setPositionRotation(this.vehicle.locX, this.vehicle.boundingBox.b + (double) this.vehicle.length, this.vehicle.locZ, this.yaw, this.pitch);
                this.vehicle.passenger = null;
            }

            this.vehicle = null;
        } else {
            if (this.vehicle != null) {
                this.vehicle.passenger = null;
            }

            if (entity != null) {
                for (Entity entity1 = entity.vehicle; entity1 != null; entity1 = entity1.vehicle) {
                    if (entity1 == this) {
                        return;
                    }
                }
            }

            this.vehicle = entity;
            entity.passenger = this;
        }
    }

    public float ae() {
        return 0.1F;
    }

    public Vec3D af() {
        return null;
    }

    public void ag() {
        if (this.portalCooldown > 0) {
            this.portalCooldown = this.ah();
        } else {
            double d0 = this.lastX - this.locX;
            double d1 = this.lastZ - this.locZ;

            if (!this.world.isStatic && !this.an) {
                this.aq = Direction.a(d0, d1);
            }

            this.an = true;
        }
    }

    public int ah() {
        return 300;
    }

    public ItemStack[] getEquipment() {
        return null;
    }

    public void setEquipment(int i, ItemStack itemstack) {}

    public boolean isBurning() {
        boolean flag = this.world != null && this.world.isStatic;

        return !this.fireProof && (this.fireTicks > 0 || flag && this.g(0));
    }

    public boolean al() {
        return this.vehicle != null;
    }

    public boolean isSneaking() {
        return this.g(1);
    }

    public void setSneaking(boolean flag) {
        this.a(1, flag);
    }

    public boolean isSprinting() {
        return this.g(3);
    }

    public void setSprinting(boolean flag) {
        this.a(3, flag);
    }

    public boolean isInvisible() {
        return this.g(5);
    }

    public void setInvisible(boolean flag) {
        this.a(5, flag);
    }

    public void e(boolean flag) {
        this.a(4, flag);
    }

    protected boolean g(int i) {
        return (this.datawatcher.getByte(0) & 1 << i) != 0;
    }

    protected void a(int i, boolean flag) {
        byte b0 = this.datawatcher.getByte(0);

        if (flag) {
            this.datawatcher.watch(0, Byte.valueOf((byte) (b0 | 1 << i)));
        } else {
            this.datawatcher.watch(0, Byte.valueOf((byte) (b0 & ~(1 << i))));
        }
    }

    public int getAirTicks() {
        return this.datawatcher.getShort(1);
    }

    public void setAirTicks(int i) {
        this.datawatcher.watch(1, Short.valueOf((short) i));
    }

    public void a(EntityLightning entitylightning) {
        this.burn(5);
        ++this.fireTicks;
        if (this.fireTicks == 0) {
            this.setOnFire(8);
        }
    }

    public void a(EntityLiving entityliving) {}

    protected boolean j(double d0, double d1, double d2) {
        int i = MathHelper.floor(d0);
        int j = MathHelper.floor(d1);
        int k = MathHelper.floor(d2);
        double d3 = d0 - (double) i;
        double d4 = d1 - (double) j;
        double d5 = d2 - (double) k;
        List list = this.world.a(this.boundingBox);

        if (list.isEmpty() && !this.world.q(i, j, k)) {
            return false;
        } else {
            boolean flag = !this.world.q(i - 1, j, k);
            boolean flag1 = !this.world.q(i + 1, j, k);
            boolean flag2 = !this.world.q(i, j - 1, k);
            boolean flag3 = !this.world.q(i, j + 1, k);
            boolean flag4 = !this.world.q(i, j, k - 1);
            boolean flag5 = !this.world.q(i, j, k + 1);
            byte b0 = 3;
            double d6 = 9999.0D;

            if (flag && d3 < d6) {
                d6 = d3;
                b0 = 0;
            }

            if (flag1 && 1.0D - d3 < d6) {
                d6 = 1.0D - d3;
                b0 = 1;
            }

            if (flag3 && 1.0D - d4 < d6) {
                d6 = 1.0D - d4;
                b0 = 3;
            }

            if (flag4 && d5 < d6) {
                d6 = d5;
                b0 = 4;
            }

            if (flag5 && 1.0D - d5 < d6) {
                d6 = 1.0D - d5;
                b0 = 5;
            }

            float f = this.random.nextFloat() * 0.2F + 0.1F;

            if (b0 == 0) {
                this.motX = (double) (-f);
            }

            if (b0 == 1) {
                this.motX = (double) f;
            }

            if (b0 == 2) {
                this.motY = (double) (-f);
            }

            if (b0 == 3) {
                this.motY = (double) f;
            }

            if (b0 == 4) {
                this.motZ = (double) (-f);
            }

            if (b0 == 5) {
                this.motZ = (double) f;
            }

            return true;
        }
    }

    public void ar() {
        this.I = true;
        this.fallDistance = 0.0F;
    }

    public String getName() {
        String s = EntityTypes.b(this);

        if (s == null) {
            s = "generic";
        }

        return LocaleI18n.get("entity." + s + ".name");
    }

    public Entity[] as() {
        return null;
    }

    public boolean i(Entity entity) {
        return this == entity;
    }

    public float getHeadRotation() {
        return 0.0F;
    }

    public boolean au() {
        return true;
    }

    public boolean j(Entity entity) {
        return false;
    }

    public String toString() {
        return String.format("%s[\'%s\'/%d, l=\'%s\', x=%.2f, y=%.2f, z=%.2f]", new Object[] { this.getClass().getSimpleName(), this.getName(), Integer.valueOf(this.id), this.world == null ? "~NULL~" : this.world.getWorldData().getName(), Double.valueOf(this.locX), Double.valueOf(this.locY), Double.valueOf(this.locZ)});
    }

    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public void k(Entity entity) {
        this.setPositionRotation(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    public void a(Entity entity, boolean flag) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        entity.e(nbttagcompound);
        this.f(nbttagcompound);
        this.portalCooldown = entity.portalCooldown;
        this.aq = entity.aq;
    }

    public void b(int i) {
        if (!this.world.isStatic && !this.dead) {
            this.world.methodProfiler.a("changeDimension");
            MinecraftServer minecraftserver = MinecraftServer.getServer();
            int j = this.dimension;
            WorldServer worldserver = minecraftserver.getWorldServer(j);
            WorldServer worldserver1 = minecraftserver.getWorldServer(i);

            this.dimension = i;
            if (j == 1 && i == 1) {
                worldserver1 = minecraftserver.getWorldServer(0);
                this.dimension = 0;
            }

            this.world.kill(this);
            this.dead = false;
            this.world.methodProfiler.a("reposition");
            minecraftserver.getPlayerList().a(this, j, worldserver, worldserver1);
            this.world.methodProfiler.c("reloading");
            Entity entity = EntityTypes.createEntityByName(EntityTypes.b(this), worldserver1);

            if (entity != null) {
                entity.a(this, true);
                if (j == 1 && i == 1) {
                    ChunkCoordinates chunkcoordinates = worldserver1.getSpawn();

                    chunkcoordinates.y = this.world.i(chunkcoordinates.x, chunkcoordinates.z);
                    entity.setPositionRotation((double) chunkcoordinates.x, (double) chunkcoordinates.y, (double) chunkcoordinates.z, entity.yaw, entity.pitch);
                }

                worldserver1.addEntity(entity);
            }

            this.dead = true;
            this.world.methodProfiler.b();
            worldserver.i();
            worldserver1.i();
            this.world.methodProfiler.b();
        }
    }

    public float a(Explosion explosion, World world, int i, int j, int k, Block block) {
        return block.a(this);
    }

    public boolean a(Explosion explosion, World world, int i, int j, int k, Block block, float f) {
        return true;
    }

    public int aw() {
        return 3;
    }

    public int ax() {
        return this.aq;
    }

    public boolean ay() {
        return false;
    }

    public void a(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.a("Entity Type", (Callable) (new CrashReportEntityType(this)));
        crashreportsystemdetails.a("Entity ID", Integer.valueOf(this.id));
        crashreportsystemdetails.a("Entity Name", (Callable) (new CrashReportEntityName(this)));
        crashreportsystemdetails.a("Entity\'s Exact location", String.format("%.2f, %.2f, %.2f", new Object[] { Double.valueOf(this.locX), Double.valueOf(this.locY), Double.valueOf(this.locZ)}));
        crashreportsystemdetails.a("Entity\'s Block location", CrashReportSystemDetails.a(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ)));
        crashreportsystemdetails.a("Entity\'s Momentum", String.format("%.2f, %.2f, %.2f", new Object[] { Double.valueOf(this.motX), Double.valueOf(this.motY), Double.valueOf(this.motZ)}));
    }

    public UUID getUniqueID() {
        return this.uniqueID;
    }

    public boolean aB() {
        return true;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(this.getName());
    }

    public void i(int i) {}
}
