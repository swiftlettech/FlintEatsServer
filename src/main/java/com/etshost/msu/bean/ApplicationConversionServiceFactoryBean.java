package com.etshost.msu.bean;

import java.text.DecimalFormat;
import java.util.Set;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

import com.etshost.msu.entity.AuthenticationRecord;
import com.etshost.msu.entity.FoodPantrySite;
import com.etshost.msu.entity.Market;
import com.etshost.msu.entity.Role;
import com.etshost.msu.entity.User;

@Configurable
/**
 * A central place to register application converters and formatters.
 */
public class ApplicationConversionServiceFactoryBean extends
		FormattingConversionServiceFactoryBean {


	static class RoleConverter implements Converter<Role, String> {
		@Override
		public String convert(final Role role) {
			return new StringBuilder().append(role.getId()).toString();
		}
	}
	
	static class MarketConverter implements Converter<Market, String> {
		@Override
		public String convert(final Market market) {
			return new StringBuilder().append(market.getId()).toString();
		}
	}

	static class FoodPantrySiteConverter implements Converter<FoodPantrySite, String> {
		@Override
		public String convert(final FoodPantrySite fps) {
			return new StringBuilder().append(fps.getId()).toString();
		}
	}

	static class UserConverter implements Converter<User, String> {
		@Override
		public String convert(final User user) {
			return new StringBuilder().append(user.getId())
					.toString();
		}
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		this.installLabelConverters(this.getObject());
	}

	public Converter<AuthenticationRecord, String> getAuthenticationRecordToStringConverter() {
		return new org.springframework.core.convert.converter.Converter<com.etshost.msu.entity.AuthenticationRecord, java.lang.String>() {
			@Override
			public String convert(
					final AuthenticationRecord authenticationRecord) {
				return new StringBuilder()
						.append(authenticationRecord.getIpAddress())
						.toString();
			}
		};
	}

	Converter<java.lang.Double, String> getDoubleConverter() {
		return new Converter<java.lang.Double, String>() {
			@Override
			public String convert(final java.lang.Double source) {
				return new StringBuilder().append(
						(new DecimalFormat("#.##").format(source))).toString();
			}
		};
	}

	public Converter<Long, AuthenticationRecord> getIdToAuthenticationRecordConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.etshost.msu.entity.AuthenticationRecord>() {
			@Override
			public com.etshost.msu.entity.AuthenticationRecord convert(
					final java.lang.Long id) {
				return AuthenticationRecord.findAuthenticationRecord(id);
			}
		};
	}
	
	public Converter<Long, Market> getIdToMarketConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.etshost.msu.entity.Market>() {
			@Override
			public com.etshost.msu.entity.Market convert(
					final java.lang.Long id) {
				return Market.findMarket(id);
			}
		};
	}

	public Converter<Long, FoodPantrySite> getIdToFoodPantrySiteConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.etshost.msu.entity.FoodPantrySite>() {
			@Override
			public com.etshost.msu.entity.FoodPantrySite convert(
					final java.lang.Long id) {
				return FoodPantrySite.findFoodPantrySite(id);
			}
		};
	}

	public Converter<Long, Role> getIdToRoleConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.etshost.msu.entity.Role>() {
			@Override
			public com.etshost.msu.entity.Role convert(
					final java.lang.Long id) {
				return Role.findRole(id);
			}
		};
	}

	public Converter<Long, User> getIdToUserConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.etshost.msu.entity.User>() {
			@Override
			public com.etshost.msu.entity.User convert(
					final java.lang.Long id) {
				return User.findUser(id);
			}
		};
	}

	Converter<Market, String> getMarketConverter() {
		return new Converter<Market, String>() {
			@Override
			public String convert(final Market source) {
				return new StringBuilder().append(source.getId()).toString();
			}
		};
	}

	public Converter<Market, String> getMarketToStringConverter() {
		return new org.springframework.core.convert.converter.Converter<com.etshost.msu.entity.Market, java.lang.String>() {
			@Override
			public String convert(final Market market) {
				return new StringBuilder().append(market.getId()).toString();
			}
		};
	}	

	
	Converter<Role, String> getRoleConverter() {
		return new Converter<Role, String>() {
			@Override
			public String convert(final Role source) {
				return new StringBuilder().append(source.getId()).toString();
			}
		};
	}

	public Converter<Role, String> getRoleToStringConverter() {
		return new org.springframework.core.convert.converter.Converter<com.etshost.msu.entity.Role, java.lang.String>() {
			@Override
			public String convert(final Role role) {
				return new StringBuilder().append(role.getId()).toString();
			}
		};
	}
	
	public Converter<String, AuthenticationRecord> getStringToAuthenticationRecordConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.String, com.etshost.msu.entity.AuthenticationRecord>() {
			@Override
			public com.etshost.msu.entity.AuthenticationRecord convert(
					final String id) {
				return ApplicationConversionServiceFactoryBean.this.getObject()
						.convert(
								ApplicationConversionServiceFactoryBean.this
										.getObject().convert(id, Long.class),
								AuthenticationRecord.class);
			}
		};
	}

	public Converter<String, Market> getStringToMarketConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.String, com.etshost.msu.entity.Market>() {
			@Override
			public com.etshost.msu.entity.Market convert(final String id) {
				return ApplicationConversionServiceFactoryBean.this.getObject()
						.convert(
								ApplicationConversionServiceFactoryBean.this
										.getObject().convert(id, Long.class),
								Market.class);
			}
		};
	}
	
	public Converter<String, Role> getStringToRoleConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.String, com.etshost.msu.entity.Role>() {
			@Override
			public com.etshost.msu.entity.Role convert(final String id) {
				return ApplicationConversionServiceFactoryBean.this.getObject()
						.convert(
								ApplicationConversionServiceFactoryBean.this
										.getObject().convert(id, Long.class),
								Role.class);
			}
		};
	}

	public Converter<String, User> getStringToUserConverter() {
		return new org.springframework.core.convert.converter.Converter<java.lang.String, com.etshost.msu.entity.User>() {
			@Override
			public com.etshost.msu.entity.User convert(final String id) {
				return ApplicationConversionServiceFactoryBean.this.getObject()
						.convert(
								ApplicationConversionServiceFactoryBean.this
										.getObject().convert(id, Long.class),
								User.class);
			}
		};
	}

	Converter<User, String> getUserConverter() {
		return new Converter<User, String>() {
			@Override
			public String convert(final User source) {
				return new StringBuilder().append(source.getId()).append(": ")
						.append(source.getName()).toString();
			}
		};
	}

	public Converter<User, String> getUserToStringConverter() {
		return new org.springframework.core.convert.converter.Converter<com.etshost.msu.entity.User, java.lang.String>() {
			@Override
			public String convert(final User user) {
				return new StringBuilder().append(user.getCreated())
						.append(' ').append(user.getModified())
						.append(user.getLogger()).toString();
			}
		};
	}

	@Override
	public void setFormatterRegistrars(Set<FormatterRegistrar> formatterRegistrars) {
		super.setFormatterRegistrars(formatterRegistrars);
	}
	
	public void installLabelConverters(final FormatterRegistry registry) {
		registry.addConverter(new MarketConverter());
		registry.addConverter(new RoleConverter());
		registry.addConverter(new UserConverter());
	}
}
